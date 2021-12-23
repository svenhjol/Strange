package svenhjol.strange.module.teleport.runic.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.strange.module.teleport.runic.RunicTeleport;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;

import java.util.Random;

@Id("strange:runic_teleport_effect")
public class ClientReceiveRunicTeleportEffect extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        RunicTeleport.Type type = buffer.readEnum(RunicTeleport.Type.class);
        BlockPos pos = buffer.readBlockPos();

        client.execute(() -> {
            ClientLevel level = client.level;
            if (level == null) return;

            var px = (double) pos.getX();
            var py = (double) pos.getY();
            var pz = (double) pos.getZ();

            Random random = level.random;
            ParticleOptions particle = type.getParticle();

            if (type == RunicTeleport.Type.RUNIC_TOME) {
                py += 0.75D;
            }

            for (int i = 0; i < 4; i++) {
                for (int x = -1; x <= 1; ++x) {
                    if (random.nextInt(2) == 0) continue;
                    for (int z = -1; z <= 1; ++z) {
                        for (int y = -1; y <= 1; ++y) {
                            level.addParticle(particle, px + 0.5D, py + 0.5D, pz + 0.5D, x + random.nextDouble() - 0.5D, y + random.nextDouble() + 0.05D, z + random.nextDouble() - 0.5D);
//                            level.addParticle(particle, playerPos.getX() + 0.5, playerPos.getY() + 0.5, playerPos.getZ() + 0.5, x + random.nextFloat() - 0.5F, y + random.nextFloat() + 0.05f, z + random.nextFloat() - 0.5F);
                        }
                    }
                }
            }
        });
    }
}
