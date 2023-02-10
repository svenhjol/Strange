package svenhjol.strange.feature.auto_recipe_unlock;

import net.minecraft.world.entity.player.Player;
import svenhjol.charm_api.event.PlayerLoginEvent;
import svenhjol.charm_core.annotation.Feature;
import svenhjol.charm_core.base.CharmFeature;
import svenhjol.strange.Strange;

@Feature(mod = Strange.MOD_ID, description = "Unlocks all vanilla recipes.")
public class AutoRecipeUnlock extends CharmFeature {
    @Override
    public void runWhenEnabled() {
        PlayerLoginEvent.INSTANCE.handle(this::handlePlayerLogin);
    }

    private void handlePlayerLogin(Player player) {
        var recipeManager = player.level.getRecipeManager();
        var allRecipes = recipeManager.getRecipes();
        player.awardRecipes(allRecipes);
    }
}
