package svenhjol.strange.module.knowledge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.knowledge.KnowledgeClient;
import svenhjol.strange.module.knowledge.branch.StructureBranch;
import svenhjol.strange.network.ClientReceiver;
import svenhjol.strange.network.Id;

import java.util.Optional;

@Id("strange:knowledge_structures")
public class ClientReceiveStructures extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = Optional.ofNullable(buffer.readNbt()).orElseThrow();

        client.execute(() -> {
            KnowledgeClient.structures = StructureBranch.load(tag);
            LogHelper.debug(getClass(), "Received " + KnowledgeClient.structures.size() + " structures from server.");
        });
    }
}
