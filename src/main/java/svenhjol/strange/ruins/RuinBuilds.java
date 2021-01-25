package svenhjol.strange.ruins;

import svenhjol.strange.ruins.builds.*;

public class RuinBuilds {
    public static void init() {
        // --- SURFACE ---
        StoneFort stoneFort = new StoneFort();
        SurfaceRuinGenerator.PLAINS_RUINS.add(stoneFort);

        // --- UNDERGROUND ---
        Castle castle = new Castle();
        UndergroundRuinGenerator.PLAINS_RUINS.add(castle);
        UndergroundRuinGenerator.FOREST_RUINS.add(castle);
        UndergroundRuinGenerator.SNOWY_RUINS.add(castle);

        Roguelike roguelike = new Roguelike();
        UndergroundRuinGenerator.FOREST_RUINS.add(roguelike);
        UndergroundRuinGenerator.SNOWY_RUINS.add(roguelike);
        UndergroundRuinGenerator.TAIGA_RUINS.add(roguelike);

        Vaults vaults = new Vaults();
        UndergroundRuinGenerator.MOUNTAINS_RUINS.add(vaults);
        UndergroundRuinGenerator.SAVANNA_RUINS.add(vaults);
        UndergroundRuinGenerator.DESERT_RUINS.add(vaults);

        // --- FOUNDATIONS ---
        StoneFoundations stoneFoundations = new StoneFoundations();
        FoundationRuinGenerator.MOUNTAINS_RUINS.add(stoneFoundations);
        FoundationRuinGenerator.BADLANDS_RUINS.add(stoneFoundations);
    }
}
