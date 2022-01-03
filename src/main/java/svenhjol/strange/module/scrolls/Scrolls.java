package svenhjol.strange.module.scrolls;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.api.event.QuestEvents;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.Quests;
import svenhjol.strange.module.runes.Tier;
import svenhjol.strange.module.scrolls.network.ServerSendDestroyScroll;
import svenhjol.strange.module.scrolls.network.ServerSendOpenScroll;

import java.util.HashMap;
import java.util.Map;

@CommonModule(mod = Strange.MOD_ID)
public class Scrolls extends CharmModule {
    public static ServerSendDestroyScroll SERVER_SEND_DESTROY_SCROLL;
    public static ServerSendOpenScroll SERVER_SEND_OPEN_SCROLL;

    public static final Map<Tier, ScrollItem> SCROLLS = new HashMap<>();

    @Override
    public void register() {
        for (Tier tier : Tier.values()) {
            SCROLLS.put(tier, new ScrollItem(this, tier));
        }

        addDependencyCheck(mod -> Strange.LOADER.isEnabled(Quests.class));
    }

    @Override
    public void runWhenEnabled() {
        QuestEvents.PAUSE.register(this::handlePauseQuest);

        SERVER_SEND_DESTROY_SCROLL = new ServerSendDestroyScroll();
        SERVER_SEND_OPEN_SCROLL = new ServerSendOpenScroll();
    }

    /**
     * When a quest is paused:
     * - give the player a scroll encoded with the quest's ID.
     */
    private void handlePauseQuest(Quest quest, ServerPlayer player) {
        Tier tier = quest.getTier();

        ItemStack scroll = new ItemStack(SCROLLS.get(tier));
        ScrollItem.setScrollQuest(scroll, quest);

        player.getInventory().placeItemBackInInventory(scroll);
    }
}
