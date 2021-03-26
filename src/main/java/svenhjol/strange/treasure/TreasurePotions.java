package svenhjol.strange.treasure;

import svenhjol.strange.treasure.potions.LegendaryPotion;

public class TreasurePotions {
    public static void init() {
        Treasure.POTIONS.put(new LegendaryPotion(), 1);
    }
}
