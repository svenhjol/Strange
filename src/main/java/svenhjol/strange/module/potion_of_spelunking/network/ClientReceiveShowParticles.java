package svenhjol.strange.module.potion_of_spelunking.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.DyeColor;
import svenhjol.charm.init.CharmParticles;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;

import java.util.Arrays;
import java.util.List;

@Id("strange:show_spelunking_particles")
public class ClientReceiveShowParticles extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        List<BlockPos> positions = Arrays.stream(buffer.readLongArray()).boxed().map(BlockPos::of).toList();
        List<DyeColor> colors = Arrays.stream(buffer.readVarIntArray()).boxed().map(DyeColor::byId).toList();

        client.execute(() -> {
            LocalPlayer player = client.player;
            ClientLevel world = client.level;

            if (player == null || world == null) return;
            BlockPos playerPos = player.blockPosition();

            for (int i = 0; i < positions.size(); i++) {
                BlockPos foundPos = positions.get(i);
                DyeColor foundColor = colors.get(i);
                float[] col = foundColor.getTextureDiffuseColors();

                BlockPos test = new BlockPos(foundPos.getX(), playerPos.getY(), foundPos.getZ());

                for (int j = -10; j < 5; j++) {
                    BlockPos to = test.relative(Direction.Axis.Y, j);
                    if (!world.getBlockState(to).isCollisionShapeFullBlock(world, to) && world.getBlockState(to.below()).isCollisionShapeFullBlock(world, to.below())) {
                        for (int k = 0; k < 2; k++) {
                            int y = to.getY() + k;
                            world.addParticle(CharmParticles.ORE_GLOW_PARTICLE, test.getX() + 0.5D, y - (k * 0.7D), foundPos.getZ() + 0.5D, col[0], col[1], col[2]);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected boolean showDebugMessages() {
        return false;
    }
}
