package svenhjol.strange.module.scrolls;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.api.event.QuestEvents;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.Quests;
import svenhjol.strange.module.runes.Tier;

import java.util.HashMap;
import java.util.Map;

@CommonModule(mod = Strange.MOD_ID)
public class Scrolls extends CharmModule {
    public static final ResourceLocation MSG_CLIENT_DESTROY_SCROLL = new ResourceLocation(Strange.MOD_ID, "client_destroy_scroll");
    public static final ResourceLocation MSG_CLIENT_OPEN_SCROLL = new ResourceLocation(Strange.MOD_ID, "client_open_scroll");

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
    }

    /**
     * When a quest is paused:
     * - give the player a scroll encoded with the quest's ID.
     */
    private void handlePauseQuest(Quest quest, ServerPlayer player) {
        String id = quest.getId();
        Tier tier = quest.getTier();

        ItemStack scroll = new ItemStack(SCROLLS.get(tier));
        ScrollItem.setScrollQuest(scroll, id);

        player.getInventory().placeItemBackInInventory(scroll);
    }
}
