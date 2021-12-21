package svenhjol.strange.module.knowledge.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.network.Id;
import svenhjol.strange.network.ServerSender;

@Id("strange:knowledge_structures")
public class ServerSendStructures extends ServerSender {
    @Override
    public void send(ServerPlayer player) {
        Knowledge.getKnowledge().ifPresent(knowledge -> {
            CompoundTag tag = new CompoundTag();
            knowledge.structureBranch.save(tag);
            send(player, buf -> buf.writeNbt(tag));
        });
    }
}
