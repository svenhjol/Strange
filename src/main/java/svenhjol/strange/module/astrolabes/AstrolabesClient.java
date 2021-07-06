package svenhjol.strange.module.astrolabes;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.DyeColor;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.init.CharmParticles;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.init.StrangeSounds;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@ClientModule(module = Astrolabes.class)
public class AstrolabesClient extends CharmModule {
    @Override
    public void register() {
        ClientPlayNetworking.registerGlobalReceiver(Astrolabes.MSG_CLIENT_SHOW_AXIS_PARTICLES, this::handleClientShowAxisParticles);
    }

    private void handleClientShowAxisParticles(Minecraft client, ClientPacketListener handler, FriendlyByteBuf data, PacketSender sender) {
        boolean playSound = data.readBoolean();
        List<BlockPos> positions = Arrays.stream(data.readLongArray()).boxed().map(BlockPos::of).collect(Collectors.toList());
        client.execute(() -> {
            ClientLevel world = client.level;
            LocalPlayer player = client.player;
            if (world == null || player == null)
                return;

            int dist = 32;
            Random random = world.random;
            boolean isClose = false;

            for (BlockPos pos : positions) {
                double px = Math.abs(pos.getX() - player.getX());
                double py = Math.abs(pos.getY() - player.getY());
                double pz = Math.abs(pos.getZ() - player.getZ());

                if (py <= dist) {
                    if (pz <= dist) {
                        for (int x = -dist; x < dist; x++) {
                            this.createAxisParticle(world, new BlockPos(player.getX() + x, pos.getY(), pos.getZ()), DyeColor.CYAN);
                        }
                        isClose = true;
                    }

                    if (px <= dist) {
                        for (int z = -dist; z < dist; z++) {
                            this.createAxisParticle(world, new BlockPos(pos.getX(), pos.getY(), player.getZ() + z), DyeColor.BLUE);
                        }
                        isClose = true;
                    }
                }

                if (px <= dist && pz <= dist) {
                    for (int y = -dist; y < dist; y++) {
                        this.createAxisParticle(world, new BlockPos(pos.getX(), player.getY() + y, pos.getZ()), DyeColor.PURPLE);
                    }
                    isClose = true;
                }
            }

            if (playSound && isClose)
                world.playSound(player, player.blockPosition(), StrangeSounds.ASTROLABE, SoundSource.PLAYERS, 0.27F, 0.8F + (0.4F * random.nextFloat()));
        });
    }

    private void createAxisParticle(ClientLevel world, BlockPos pos, DyeColor color) {
        SimpleParticleType particleType = CharmParticles.AXIS_PARTICLE;

        float[] col = color.getTextureDiffuseColors();
        double x = (double) pos.getX() + 0.5D;
        double y = (double) pos.getY() + 0.5D;
        double z = (double) pos.getZ() + 0.5D;

        world.addAlwaysVisibleParticle(particleType, x, y, z, col[0], col[1], col[2]);
    }
}
