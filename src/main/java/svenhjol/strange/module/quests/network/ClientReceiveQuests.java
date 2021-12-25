package svenhjol.strange.module.quests.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.Strange;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.QuestData;
import svenhjol.strange.module.quests.QuestsClient;

@Id("strange:quests")
public class ClientReceiveQuests extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = getCompoundTag(buffer).orElseThrow();

        client.execute(() -> {
            QuestsClient.quests.clear();
            ListTag listTag = tag.getList(QuestData.QUESTS_TAG, 10);

            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag questTag = listTag.getCompound(i);
                QuestsClient.quests.add(Quest.load(questTag));
            }

            LogHelper.debug(Strange.MOD_ID, getClass(), "Received " + QuestsClient.quests.size() + " quests.");
        });
    }
}
