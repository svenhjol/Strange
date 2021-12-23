package svenhjol.strange.module.scrolls.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;

import java.util.Random;

@Id("strange:open_scroll")
public class ClientReceiveOpenScroll extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        Player player = client.player;
        if (player == null) return;

        client.execute(() -> {
            double spread = 1.1D;
            Random random = player.level.random;
            for (int i = 0; i < 40; i++) {
                double px = player.blockPosition().getX() + ((random.nextFloat()*2) - (random.nextFloat()*2)) * spread;
                double py = player.blockPosition().getY() + 0.5D;
                double pz = player.blockPosition().getZ() + ((random.nextFloat()*2) - (random.nextFloat()*2)) * spread;
                player.level.addParticle(ParticleTypes.ENCHANT, px, py, pz, 0.0D, 0.0D, 0.0D);
            }
        });
    }
}
