package svenhjol.strange.module.quests.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;
import svenhjol.strange.module.quests.Quests;
import svenhjol.strange.module.quests.definition.QuestDefinition;
import svenhjol.strange.module.runes.Tier;

import java.util.Map;

@Id("strange:quest_definitions")
public class ServerSendQuestDefinitions extends ServerSender {
    @Override
    public void send(ServerPlayer player) {
        var tag = new CompoundTag();

        for (Map.Entry<Tier, Map<String, QuestDefinition>> entry : Quests.DEFINITIONS.entrySet()) {
            var tier = entry.getKey();
            var tag1 = new CompoundTag();

            for (Map.Entry<String, QuestDefinition> tierEntry : entry.getValue().entrySet()) {
                var id = tierEntry.getKey();
                var definition = tierEntry.getValue();

                tag1.put(id, definition.save());
            }

            tag.put(tier.getSerializedName(), tag1);
        }

        super.send(player, buf -> buf.writeNbt(tag));
    }
}
