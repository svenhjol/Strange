package svenhjol.strange.treasure;

import svenhjol.strange.module.Treasure;
import svenhjol.strange.treasure.potion.LegendaryPotion;

public class TreasurePotions {
    public static void init() {
        Treasure.POTIONS.put(new LegendaryPotion(), 1);
    }
}
