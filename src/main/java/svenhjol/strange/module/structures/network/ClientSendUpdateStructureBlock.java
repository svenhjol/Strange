package svenhjol.strange.module.structures.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import svenhjol.charm.network.ClientSender;
import svenhjol.charm.network.Id;

@Id("strange:update_structure_block")
public class ClientSendUpdateStructureBlock extends ClientSender {
    public void send(BlockEntity blockEntity, BlockPos pos) {
        super.send(buf -> {
            buf.writeBlockPos(pos);
            buf.writeNbt(blockEntity.saveWithoutMetadata());
        });
    }
}
