package svenhjol.strange.feature.cooking_pots;

import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.Item;

public class MixedStewItem extends BowlFoodItem {
    public MixedStewItem() {
        super(new Item.Properties()
            .stacksTo(CookingPots.getStewStackSize())
            .food(CookingPots.mixedStewFoodProperties));
    }
}
