package svenhjol.strange.module.treasure;

import svenhjol.strange.module.treasure.potion.LegendaryPotion;

public class TreasurePotions {
    public static void init() {
        Treasure.POTIONS.put(new LegendaryPotion(), 1);
    }
}
