package svenhjol.strange.treasure;

import svenhjol.strange.treasure.tools.*;

public class TreasureTools {
    public static void init() {
        // TODO: config to enable/disable each item/group
        Treasure.TOOLS.put(new LegendaryAxe(), 4);
        Treasure.TOOLS.put(new LegendaryBoots(), 3);
        Treasure.TOOLS.put(new LegendaryBow(), 4);
        Treasure.TOOLS.put(new LegendaryChestplate(), 3);
        Treasure.TOOLS.put(new LegendaryCrossbow(), 4);
        Treasure.TOOLS.put(new LegendaryFishingRod(), 2);
        Treasure.TOOLS.put(new LegendaryHelmet(), 3);
        Treasure.TOOLS.put(new LegendaryLeggings(), 3);
        Treasure.TOOLS.put(new LegendaryPickaxe(), 4);
        Treasure.TOOLS.put(new LegendaryShield(), 5);
        Treasure.TOOLS.put(new LegendaryShovel(), 4);
        Treasure.TOOLS.put(new LegendarySword(), 4);
        Treasure.TOOLS.put(new LegendaryTrident(), 2);
        Treasure.TOOLS.put(new AngeryPotato(), 1);
        Treasure.TOOLS.put(new AmbitiousCrossbow(), 1);
        Treasure.TOOLS.put(new EldritchBow(), 1);
        Treasure.TOOLS.put(new NeedleSword(), 1);
        Treasure.TOOLS.put(new WyvernAxe(), 1);
    }
}
