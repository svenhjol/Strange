package svenhjol.strange.totems;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.PlayerHelper;
import svenhjol.charm.base.iface.Module;
import svenhjol.charm.event.ApplyBeaconEffectsCallback;
import svenhjol.charm.event.EntityJumpCallback;
import svenhjol.charm.event.PlayerTickCallback;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Module(mod = Strange.MOD_ID, client = TotemOfFlyingClient.class, description = "A Totem of Flying lets you fly within range of an active beacon.")
public class TotemOfFlying extends CharmModule {
    public static ItemStack cachedTotemItemStack;
    public static TotemOfFlyingItem TOTEM_OF_FLYING;
    public static List<UUID> playersInRange = new ArrayList<>();
    public static List<UUID> flyingPlayers = new ArrayList<>();

    @Override
    public void register() {
        TOTEM_OF_FLYING = new TotemOfFlyingItem(this);
    }

    @Override
    public void init() {
        cachedTotemItemStack = new ItemStack(TOTEM_OF_FLYING);
        PlayerTickCallback.EVENT.register(this::handlePlayerTick);
        EntityJumpCallback.EVENT.register(this::handleEntityJump);
        ApplyBeaconEffectsCallback.EVENT.register(this::handleBeaconEffects);
    }

    private void handleBeaconEffects(World world, BlockPos pos, int levels, StatusEffect primaryEffect, StatusEffect secondaryEffect) {
        if (!world.isClient && world.getTime() % 5 == 0) {
            double d0 = levels * 10 + 10;
            Box bb = (new Box(pos)).expand(d0).expand(0.0D, world.getTopY(), 0.0D);

            List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, bb);
            playersInRange.clear();
            list.forEach(player -> playersInRange.add(player.getUuid()));
        }
    }

    private void handleEntityJump(LivingEntity entity) {
        if (!(entity instanceof PlayerEntity))
            return;

        PlayerEntity player = (PlayerEntity)entity;
        boolean hasTotem = PlayerHelper.getInventory(player).contains(cachedTotemItemStack);
        if (!hasTotem)
            return;

        enableFlight(player);
    }

    private void handlePlayerTick(PlayerEntity player) {
        if (player.world.getTime() % 4 != 0)
            return;

        UUID uuid = player.getUuid();
        boolean isFlying = flyingPlayers.contains(uuid);
        boolean hasTotem = PlayerHelper.getInventory(player).contains(cachedTotemItemStack);

        if (!hasTotem) {
            if (isFlying) {
                disableFlight(player);
            }
            return;
        }

        if (playersInRange.contains(uuid)) {
            player.getAbilities().allowFlying = true;
        } else {
            disableFlight(player);
        }
    }

    private void disableFlight(PlayerEntity player) {
        if (player.isCreative() || player.isSpectator()) {
            player.getAbilities().allowFlying = true;
        } else {
            player.getAbilities().flying = false;
            player.getAbilities().allowFlying = false;
            flyingPlayers.remove(player.getUuid());
        }
        player.sendAbilitiesUpdate();
    }

    private void enableFlight(PlayerEntity player) {
        if (player.isCreative() || player.isSpectator()) {
            player.getAbilities().allowFlying = true;
        } else {
            player.getAbilities().flying = true;
            player.getAbilities().allowFlying = true;
            flyingPlayers.add(player.getUuid());
        }
        player.sendAbilitiesUpdate();
    }
}
