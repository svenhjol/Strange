package svenhjol.strange.feature.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.feature.advancements.Advancements;
import svenhjol.charmony.api.event.PlayerLoginEvent;
import svenhjol.strange.Strange;

public class Core extends CommonFeature {
    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public void runWhenEnabled() {
        PlayerLoginEvent.INSTANCE.handle(this::handlePlayerLogin);
    }

    private void handlePlayerLogin(Player player) {
        Advancements.trigger(new ResourceLocation(Strange.ID, "player_joined"), player);
    }
}
