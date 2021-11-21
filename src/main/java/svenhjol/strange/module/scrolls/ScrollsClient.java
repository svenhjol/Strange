package svenhjol.strange.module.scrolls;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;

import java.util.Random;

@ClientModule(module = Scrolls.class)
public class ScrollsClient extends CharmModule {
    @Override
    public void runWhenEnabled() {
        ClientPlayNetworking.registerGlobalReceiver(Scrolls.MSG_CLIENT_DESTROY_SCROLL, this::handleDestroyScroll);
        ClientPlayNetworking.registerGlobalReceiver(Scrolls.MSG_CLIENT_OPEN_SCROLL, this::handleOpenScroll);
    }

    private void handleDestroyScroll(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        client.execute(() -> {
            Player player = client.player;
            if (player == null) return;

            double spread = 1.1D;
            Random random = player.level.random;
            for (int i = 0; i < 40; i++) {
                double px = player.blockPosition().getX() + ((random.nextFloat()*2) - (random.nextFloat()*2)) * spread;
                double py = player.blockPosition().getY() + 0.5D;
                double pz = player.blockPosition().getZ() + ((random.nextFloat()*2) - (random.nextFloat()*2)) * spread;
                player.level.addParticle(ParticleTypes.SMOKE, px, py, pz, 0.0D, 0.0D, 0.0D);
            }
        });
    }

    private void handleOpenScroll(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        client.execute(() -> {
            Player player = client.player;
            if (player == null) return;

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
