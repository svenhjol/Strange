package svenhjol.strange.totems.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import svenhjol.meson.helper.ClientHelper;

@OnlyIn(Dist.CLIENT)
public class TotemOfFlyingClient
{
    @OnlyIn(Dist.CLIENT)
    public void disableFlight()
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
    public void enableFlight()
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
    public void effectStartFlying(PlayerEntity player)
    {
        Minecraft.getInstance().getSoundHandler().play(new TotemOfFlyingSound((ClientPlayerEntity)player));
    }

    @OnlyIn(Dist.CLIENT)
    public void effectFlying(PlayerEntity player)
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
