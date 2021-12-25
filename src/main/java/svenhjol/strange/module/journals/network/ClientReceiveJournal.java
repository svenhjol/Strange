package svenhjol.strange.module.journals.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.Strange;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.JournalsClient;

@Id("strange:journal")
public class ClientReceiveJournal extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = getCompoundTag(buffer).orElseThrow();

        client.execute(() -> {
            var journal = JournalData.load(tag);
            JournalsClient.setJournal(journal);

            LogHelper.debug(Strange.MOD_ID, getClass(), "Journal: " +
                journal.getLearnedRunes().size() + " learned runes, " +
                journal.getLearnedBiomes().size() + " learned biomes, " +
                journal.getLearnedDimensions().size() + " learned dimensions, " +
                journal.getLearnedStructures().size() + " learned structures.");
        });
    }
}
