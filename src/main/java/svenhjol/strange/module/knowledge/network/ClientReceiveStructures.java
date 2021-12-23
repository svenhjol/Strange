package svenhjol.strange.module.knowledge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.knowledge.KnowledgeClient;
import svenhjol.strange.module.knowledge.branch.StructureBranch;

import java.util.Optional;

@Id("strange:knowledge_structures")
public class ClientReceiveStructures extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = Optional.ofNullable(buffer.readNbt()).orElseThrow();

        client.execute(() -> {
            var branch = StructureBranch.load(tag);
            KnowledgeClient.setStructures(branch);
            LogHelper.debug(getClass(), "Received " + branch.size() + " structures from server.");
        });
    }
}
