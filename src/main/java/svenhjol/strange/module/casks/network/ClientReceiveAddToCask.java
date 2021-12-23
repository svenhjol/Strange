package svenhjol.strange.module.casks.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;

import java.util.Random;

@Id("strange:add_to_cask")
public class ClientReceiveAddToCask extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        if (client.level == null) return;
        BlockPos pos = buffer.readBlockPos();

        client.execute(() -> {
            Random random = client.level.getRandom();
            for(int i = 0; i < 10; ++i) {
                double g = random.nextGaussian() * 0.02D;
                double h = random.nextGaussian() * 0.02D;
                double j = random.nextGaussian() * 0.02D;
                client.level.addParticle(ParticleTypes.SMOKE, (double)pos.getX() + 0.13 + (0.73D * (double)random.nextFloat()), (double)pos.getY() + 0.8D + (double)random.nextFloat() * 0.3D, (double)pos.getZ() + 0.13D + (0.73 * (double)random.nextFloat()), g, h, j);
            }
        });
    }
}
