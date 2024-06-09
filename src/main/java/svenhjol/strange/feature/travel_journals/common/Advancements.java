package svenhjol.strange.feature.travel_journals.common;

import net.minecraft.world.entity.player.Player;
import svenhjol.charm.feature.core.custom_advancements.common.AdvancementHolder;
import svenhjol.strange.feature.travel_journals.TravelJournals;

public class Advancements extends AdvancementHolder<TravelJournals> {
    public Advancements(TravelJournals feature) {
        super(feature);
    }
    
    public void madeTravelBookmark(Player player) {
        trigger("made_travel_bookmark", player);
    }
}
