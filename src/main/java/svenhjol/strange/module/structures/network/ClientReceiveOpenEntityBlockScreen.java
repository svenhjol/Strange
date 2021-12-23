package svenhjol.strange.module.structures.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.structures.EntityBlockEntity;
import svenhjol.strange.module.structures.screen.EntityBlockScreen;

@Id("strange:open_entity_block_screen")
public class ClientReceiveOpenEntityBlockScreen extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        BlockPos blockPos = buffer.readBlockPos();
        if (client.player == null || client.player.level == null) return;

        client.execute(() -> {
            Player player = client.player;
            Level level = player.level;

            if (level.getBlockEntity(blockPos) instanceof EntityBlockEntity blockEntity) {
                client.setScreen(new EntityBlockScreen(blockPos, blockEntity));
            }
        });
    }
}
