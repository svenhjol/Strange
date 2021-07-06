package svenhjol.strange.module.totem_of_flying;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.event.ApplyBeaconEffectsCallback;
import svenhjol.charm.event.EntityJumpCallback;
import svenhjol.charm.event.PlayerTickCallback;
import svenhjol.charm.helper.PlayerHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CommonModule(mod = Strange.MOD_ID, description = "A Totem of Flying lets you fly within range of an active beacon.")
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
    public void runWhenEnabled() {
        cachedTotemItemStack = new ItemStack(TOTEM_OF_FLYING);
        PlayerTickCallback.EVENT.register(this::handlePlayerTick);
        EntityJumpCallback.EVENT.register(this::handleEntityJump);
        ApplyBeaconEffectsCallback.EVENT.register(this::handleBeaconEffects);
    }

    private void handleBeaconEffects(Level world, BlockPos pos, int levels, MobEffect primaryEffect, MobEffect secondaryEffect) {
        if (!world.isClientSide && world.getGameTime() % 5 == 0) {
            double d0 = levels * 10 + 10;
            AABB bb = (new AABB(pos)).inflate(d0).inflate(0.0D, world.getMaxBuildHeight(), 0.0D);

            List<Player> list = world.getEntitiesOfClass(Player.class, bb);
            playersInRange.clear();
            list.forEach(player -> playersInRange.add(player.getUUID()));
        }
    }

    private void handleEntityJump(LivingEntity entity) {
        if (!(entity instanceof Player player))
            return;

        boolean hasTotem = PlayerHelper.getInventory(player).contains(cachedTotemItemStack);
        if (!hasTotem)
            return;

        enableFlight(player);
    }

    private void handlePlayerTick(Player player) {
        if (player.level.getGameTime() % 4 != 0)
            return;

        UUID uuid = player.getUUID();
        boolean isFlying = flyingPlayers.contains(uuid);
        boolean hasTotem = PlayerHelper.getInventory(player).contains(cachedTotemItemStack);

        if (!hasTotem) {
            if (isFlying) {
                disableFlight(player);
            }
            return;
        }

        if (playersInRange.contains(uuid)) {
            player.getAbilities().mayfly = true;
        } else {
            disableFlight(player);
        }
    }

    private void disableFlight(Player player) {
        if (player.isCreative() || player.isSpectator()) {
            player.getAbilities().mayfly = true;
        } else {
            player.getAbilities().flying = false;
            player.getAbilities().mayfly = false;
            flyingPlayers.remove(player.getUUID());
        }
        player.onUpdateAbilities();
    }

    private void enableFlight(Player player) {
        if (player.isCreative() || player.isSpectator()) {
            player.getAbilities().mayfly = true;
        } else {
            player.getAbilities().flying = true;
            player.getAbilities().mayfly = true;
            flyingPlayers.add(player.getUUID());
        }
        player.onUpdateAbilities();
    }
}
