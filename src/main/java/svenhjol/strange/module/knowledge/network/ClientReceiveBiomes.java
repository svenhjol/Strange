package svenhjol.strange.module.knowledge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.knowledge.KnowledgeClient;
import svenhjol.strange.module.knowledge.branch.BiomeBranch;
import svenhjol.strange.network.ClientReceiver;
import svenhjol.strange.network.Id;

import java.util.Optional;

@Id("strange:knowledge_biomes")
public class ClientReceiveBiomes extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = Optional.ofNullable(buffer.readNbt()).orElseThrow();

        client.execute(() -> {
            KnowledgeClient.biomes = BiomeBranch.load(tag);
            LogHelper.debug(getClass(), "Received " + KnowledgeClient.biomes.size() + " biomes from server.");
        });
    }
}
