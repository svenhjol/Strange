package svenhjol.strange.module.structures.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.structures.DataBlockEntity;
import svenhjol.strange.module.structures.screen.DataBlockScreen;

@Id("strange:open_data_block_screen")
public class ClientReceiveOpenDataBlockScreen extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        BlockPos blockPos = buffer.readBlockPos();
        if (client.player == null || client.player.level == null) return;

        client.execute(() -> {
            Player player = client.player;
            Level level = player.level;

            if (level.getBlockEntity(blockPos) instanceof DataBlockEntity data) {
                client.setScreen(new DataBlockScreen(blockPos, data));
            }
        });
    }
}
