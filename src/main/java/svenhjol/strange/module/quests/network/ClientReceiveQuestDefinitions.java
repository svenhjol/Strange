package svenhjol.strange.module.quests.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.quests.Quests;
import svenhjol.strange.module.quests.definition.QuestDefinition;
import svenhjol.strange.module.runes.Tier;

import java.util.HashMap;

@Id("strange:quest_definitions")
public class ClientReceiveQuestDefinitions extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = getCompoundTag(buffer).orElseThrow();

        client.execute(() -> {
            Quests.DEFINITIONS.clear();
            int count = 0;

            for (String t : tag.getAllKeys()) {
                var tier = Tier.byName(t);
                var tag1 = tag.getCompound(t);

                for (String id : tag1.getAllKeys()) {
                    var d = tag1.getCompound(id);
                    var definition = QuestDefinition.load(d);

                    count++;
                    Quests.DEFINITIONS.computeIfAbsent(tier, m -> new HashMap<>()).put(id, definition);
                }
            }
            LogHelper.debug(getClass(), "Received " + count + " quest definitions.");
        });
    }
}
