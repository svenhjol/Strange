package svenhjol.strange.totems.module;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.helper.ClientHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.StrangeHelper;
import svenhjol.strange.base.message.DamageTotemOfFlying;
import svenhjol.strange.totems.item.TotemOfFlyingItem;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfFlying extends MesonModule
{
    public static TotemOfFlyingItem item;

    @Config(name = "Durability", description = "Durability of the Totem.")
    public static int durability = 120;

    @Override
    public void init()
    {
        item = new TotemOfFlyingItem(this);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START
            && event.player != null
            && event.player.world.getGameTime() % 5 == 0
        ) {
            PlayerEntity player = event.player;
            BlockPos playerPos = player.getPosition();

            if (player.getHeldItemMainhand().getItem() == item
                || player.getHeldItemOffhand().getItem() == item) {

                if (!player.abilities.isFlying && player.world.getBlockState(playerPos.down()).getBlock() == Blocks.BEACON) {
                    enableFlight(player);
                }

                if (player.abilities.isFlying) {

                    if (!player.world.isRemote && player.world.rand.nextFloat() < 0.15F) {
                        for (Hand hand : Hand.values()) {
                            ItemStack held = player.getHeldItem(hand);
                            if (held.getItem() != item) continue;
                            PacketHandler.sendTo(new DamageTotemOfFlying(playerPos), (ServerPlayerEntity) player);
                            int damage = StrangeHelper.damageTotem(player, held, 1);

                            if (damage > held.getMaxDamage()) {
                                StrangeHelper.destroyTotem(player, held);
                                disableFlight(player);
                                return;
                            }
                        }
                    }

                    if (player.world.isRemote) {
                        effectFlying(playerPos);
                    }

                    return;
                }
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
            player.abilities.isFlying = true;
            player.abilities.allowFlying = true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void effectFlying(BlockPos pos)
    {
        double spread = 0.6D;
        for (int i = 0; i < 8; i++) {
            double px = pos.getX() + 0.25D + (Math.random() - 0.5D) * spread;
            double py = pos.getY() - 0.4D + (Math.random() - 0.5D) * spread;
            double pz = pos.getZ() + 0.25D + (Math.random() - 0.5D) * spread;
            ClientHelper.getClientWorld().addParticle(ParticleTypes.CLOUD, px, py, pz, 0.0D, 0.3D, 0.0D);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void effectDamageTotem(BlockPos pos)
    {
        double spread = 1.5D;
        for (int i = 0; i < 8; i++) {
            double px = pos.getX() + 0.25D + (Math.random() - 0.5D) * spread;
            double py = pos.getY() + 0.25D + (Math.random() - 0.5D) * spread;
            double pz = pos.getZ() + 0.25D + (Math.random() - 0.5D) * spread;
            ClientHelper.getClientWorld().addParticle(ParticleTypes.SMOKE, px, py, pz, 0.0D, 0.3D, 0.0D);
        }
    }
}
