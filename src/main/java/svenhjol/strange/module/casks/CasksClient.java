package svenhjol.strange.module.casks;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;

import java.util.Random;

@ClientModule(module = Casks.class)
public class CasksClient extends CharmModule {

    @Override
    public void register() {
        ClientPlayNetworking.registerGlobalReceiver(Casks.MSG_CLIENT_ADDED_TO_CASK, this::handleClientAddedToCask);
    }

    private void handleClientAddedToCask(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        BlockPos pos = BlockPos.of(buffer.readLong());
        client.execute(() -> {
            if (client.level != null) {
                createParticles(client.level, pos);
            }
        });
    }

    private void createParticles(Level level, BlockPos pos) {
        Random random = level.getRandom();
        for(int i = 0; i < 10; ++i) {
            double g = random.nextGaussian() * 0.02D;
            double h = random.nextGaussian() * 0.02D;
            double j = random.nextGaussian() * 0.02D;
            level.addParticle(ParticleTypes.SMOKE, (double)pos.getX() + 0.13 + (0.73D * (double)random.nextFloat()), (double)pos.getY() + 0.8D + (double)random.nextFloat() * 0.3D, (double)pos.getZ() + 0.13D + (0.73 * (double)random.nextFloat()), g, h, j);
        }
    }
}
