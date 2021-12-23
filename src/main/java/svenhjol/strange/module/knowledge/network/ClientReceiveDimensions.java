package svenhjol.strange.module.knowledge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.knowledge.KnowledgeClient;
import svenhjol.strange.module.knowledge.branch.DimensionBranch;

import java.util.Optional;

@Id("strange:knowledge_dimensions")
public class ClientReceiveDimensions extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = Optional.ofNullable(buffer.readNbt()).orElseThrow();

        client.execute(() -> {
            var branch = DimensionBranch.load(tag);
            KnowledgeClient.setDimensions(branch);
            LogHelper.debug(getClass(), "Dimensions branch has " + branch.size() + " dimensions.");
        });
    }
}
