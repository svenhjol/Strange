package svenhjol.strange.module.knowledge.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;

@Id("strange:knowledge_dimensions")
public class ServerSendDimensions extends ServerSender {
    @Override
    public void send(ServerPlayer player) {
        Knowledge.getKnowledge().ifPresent(knowledge -> {
            CompoundTag tag = new CompoundTag();
            knowledge.dimensionBranch.save(tag);
            send(player, buf -> buf.writeNbt(tag));
        });
    }
}
