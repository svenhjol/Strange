package svenhjol.strange.ruins;

import svenhjol.strange.ruins.builds.Castle;
import svenhjol.strange.ruins.builds.Roguelike;
import svenhjol.strange.ruins.builds.StoneRoom;
import svenhjol.strange.ruins.builds.Vaults;

public class RuinBuilds {
    public static void init() {
        Castle castle = new Castle();
        UndergroundRuinGenerator.PLAINS_RUINS.add(castle);
        UndergroundRuinGenerator.FOREST_RUINS.add(castle);
        UndergroundRuinGenerator.SNOWY_RUINS.add(castle);

        Roguelike roguelike = new Roguelike();
        UndergroundRuinGenerator.FOREST_RUINS.add(roguelike);
        UndergroundRuinGenerator.SNOWY_RUINS.add(roguelike);
        UndergroundRuinGenerator.TAIGA_RUINS.add(roguelike);

        StoneRoom stoneRoom = new StoneRoom();
        FoundationRuinGenerator.MOUNTAINS_RUINS.add(stoneRoom);

        Vaults vaults = new Vaults();
        UndergroundRuinGenerator.MOUNTAINS_RUINS.add(vaults);
        UndergroundRuinGenerator.DESERT_RUINS.add(vaults);
        UndergroundRuinGenerator.SAVANNA_RUINS.add(vaults);
    }
}
