package svenhjol.strange.totems.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.helper.ClientHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.TotemHelper;
import svenhjol.strange.totems.message.ClientTotemUpdateFlying;
import svenhjol.strange.totems.client.TotemOfFlyingSound;
import svenhjol.strange.totems.item.TotemOfFlyingItem;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfFlying extends MesonModule
{
    public static TotemOfFlyingItem item;

    @Config(name = "Durability", description = "Durability of the Totem.")
    public static int durability = 32;

    @Config(name = "XP cost", description = "Amount of XP consumed every second (20 ticks) while flying.")
    public static int xpCost = 1;

    @Override
    public void init()
    {
        item = new TotemOfFlyingItem(this);
    }

    @SubscribeEvent
    public void onJump(LivingJumpEvent event)
    {
        if (event.getEntityLiving() instanceof PlayerEntity
            && (event.getEntityLiving().getHeldItemMainhand().getItem() == item
            || event.getEntityLiving().getHeldItemOffhand().getItem() == item)
        ) {
            PlayerEntity player = (PlayerEntity)event.getEntityLiving();
            ItemStack held = null;

            if (player.experienceTotal <= 0) return;

            for (Hand hand : Hand.values()) {
                if (player.getHeldItem(hand).getItem() == item) {
                    held = player.getHeldItem(hand);
                }
            }
            if (held == null) return;

            TotemHelper.damageOrDestroy(player, held, 1);
            if (player.world.isRemote) {
                effectStartFlying(player);
            }
            enableFlight(player);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START
            && event.player != null
            && event.player.world.getGameTime() % 4 == 0
        ) {
            PlayerEntity player = event.player;
            World world = player.world;

            if (player.getHeldItemMainhand().getItem() == item || player.getHeldItemOffhand().getItem() == item) {
                if (player.abilities.isFlying) {
                    if (!world.isRemote && world.getGameTime() % 20 == 0) {
                        int xp = player.experienceTotal;
                        if (xp <= 0) {
                            disableFlight(player);
                            PacketHandler.sendTo(new ClientTotemUpdateFlying(ClientTotemUpdateFlying.DISABLE), (ServerPlayerEntity) player);
                            return;
                        }

                        for (Hand hand : Hand.values()) {
                            ItemStack held = player.getHeldItem(hand);
                            if (held.getItem() != item) continue;
                            player.giveExperiencePoints(-xpCost);
                        }
                    }
                    if (world.isRemote) {
                        effectFlying(player);
                    }
                }
                return;
            }

            disableFlight(player);
        }
    }

    private void disableFlight(PlayerEntity player)
    {
        if (player.isCreative() || player.isSpectator()) {
            player.abilities.allowFlying = true;
        } else{
            player.abilities.isFlying = false;
            player.abilities.allowFlying = false;
        }
    }

    private void enableFlight(PlayerEntity player)
    {
        if (player.isCreative() || player.isSpectator()) {
            player.abilities.allowFlying = true;
        } else {
            player.abilities.allowFlying = true;
            player.abilities.isFlying = true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void disableFlight()
    {
        PlayerEntity player = ClientHelper.getClientPlayer();
        if (player.isCreative() || player.isSpectator()) {
            player.abilities.allowFlying = true;
        } else{
            player.abilities.isFlying = false;
            player.abilities.allowFlying = false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void enableFlight()
    {
        PlayerEntity player = ClientHelper.getClientPlayer();
        if (player.isCreative() || player.isSpectator()) {
            player.abilities.allowFlying = true;
        } else {
            player.abilities.allowFlying = true;
            player.abilities.isFlying = true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void effectStartFlying(PlayerEntity player)
    {
        Minecraft.getInstance().getSoundHandler().play(new TotemOfFlyingSound((ClientPlayerEntity)player));
    }

    @OnlyIn(Dist.CLIENT)
    public static void effectFlying(PlayerEntity player)
    {
        double spread = 0.6D;
        BlockPos pos = player.getPosition();
        for (int i = 0; i < 8; i++) {
            double px = pos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
            double py = pos.getY() - 0.4D + (Math.random() - 0.5D) * spread;
            double pz = pos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
            ClientHelper.getClientWorld().addParticle(ParticleTypes.CLOUD, px, py, pz, 0.0D, 0.1D, 0.0D);
        }
    }
}
