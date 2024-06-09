package svenhjol.strange.feature.runestones.common;

import net.minecraft.world.entity.player.Player;
import svenhjol.charm.feature.core.custom_advancements.common.AdvancementHolder;
import svenhjol.strange.feature.runestones.Runestones;

public final class Advancements extends AdvancementHolder<Runestones> {
    public Advancements(Runestones feature) {
        super(feature);
    }
    
    public void lookedAtRunestone(Player player) {
        trigger("looked_at_runestone", player);
    }
    
    public void activatedRunestone(Player player) {
        trigger("activated_runestone", player);
    }
    
    public void travelledViaRunestone(Player player) {
        trigger("travelled_via_runestone", player);
    }
    
    public void travelledHomeViaRunestone(Player player) {
        trigger("travelled_home_via_runestone", player);
    }
}
