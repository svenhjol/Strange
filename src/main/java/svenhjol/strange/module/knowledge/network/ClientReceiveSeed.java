package svenhjol.strange.module.knowledge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.network.ClientReceiver;
import svenhjol.strange.network.Id;

@Id("strange:knowledge_seed")
public class ClientReceiveSeed extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var seed = buffer.readLong();

        client.execute(() -> {
            Knowledge.SEED = seed;
            LogHelper.debug(getClass(), "Received seed " + seed + " from server.");
        });
    }
}
