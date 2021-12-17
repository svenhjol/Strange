package svenhjol.strange.module.teleport;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;

import java.util.Random;

@ClientModule(module = Teleport.class)
public class TeleportClient extends CharmModule {
    @Override
    public void runWhenEnabled() {
        ClientPlayNetworking.registerGlobalReceiver(Teleport.MSG_CLIENT_TELEPORT_EFFECT, this::handleTeleportEffect);
    }

    private void handleTeleportEffect(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        Teleport.Type type = buffer.readEnum(Teleport.Type.class);
        BlockPos pos = buffer.readBlockPos();

        client.execute(() -> {
            ClientLevel level = client.level;
            if (level == null) return;

            Random random = level.random;
            ParticleOptions particle = type.getParticle();

            for (int i = 0; i < 4; i++) {
                for (int x = -1; x <= 1; ++x) {
                    if (random.nextInt(2) == 0) continue;
                    for (int z = -1; z <= 1; ++z) {
                        for (int y = -1; y <= 1; ++y) {
                            level.addParticle(particle, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, x + random.nextFloat() - 0.5F, y + random.nextFloat() + 0.05f, z + random.nextFloat() - 0.5F);
//                            level.addParticle(particle, playerPos.getX() + 0.5, playerPos.getY() + 0.5, playerPos.getZ() + 0.5, x + random.nextFloat() - 0.5F, y + random.nextFloat() + 0.05f, z + random.nextFloat() - 0.5F);
                        }
                    }
                }
            }
        });
    }
}
