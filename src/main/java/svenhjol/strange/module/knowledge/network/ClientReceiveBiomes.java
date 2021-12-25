package svenhjol.strange.module.knowledge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.Strange;
import svenhjol.strange.module.knowledge.KnowledgeClient;
import svenhjol.strange.module.knowledge.branch.BiomeBranch;

import java.util.Optional;

@Id("strange:knowledge_biomes")
public class ClientReceiveBiomes extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = Optional.ofNullable(buffer.readNbt()).orElseThrow();

        client.execute(() -> {
            var branch = BiomeBranch.load(tag);
            KnowledgeClient.setBiomes(branch);
            LogHelper.debug(Strange.MOD_ID, getClass(), "Biomes branch has " + branch.size() + " biomes.");
        });
    }
}
