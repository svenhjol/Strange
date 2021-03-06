package svenhjol.strange.module.ruins;

import svenhjol.strange.module.ruins.builds.*;
import svenhjol.strange.module.ruins.generator.CaveRuinGenerator;
import svenhjol.strange.module.ruins.generator.DeepRuinGenerator;
import svenhjol.strange.module.ruins.generator.SurfaceRuinGenerator;

public class RuinBuilds {
    public static void init() {
        // --- SURFACE ---
        StoneFort stoneFort = new StoneFort();
        SurfaceRuinGenerator.RUINS.add(stoneFort);

        // --- CAVE ---
        Castle castle = new Castle();
        CaveRuinGenerator.RUINS.add(castle);
        CaveRuinGenerator.RUINS.add(castle);
        CaveRuinGenerator.RUINS.add(castle);

        Roguelike roguelike = new Roguelike();
        CaveRuinGenerator.RUINS.add(roguelike);
        CaveRuinGenerator.RUINS.add(roguelike);
        CaveRuinGenerator.RUINS.add(roguelike);

        Vaults vaults = new Vaults();
        CaveRuinGenerator.RUINS.add(vaults);
        CaveRuinGenerator.RUINS.add(vaults);
        CaveRuinGenerator.RUINS.add(vaults);

        // --- DEEP ---
        StoneRoom stoneRoom = new StoneRoom();
        DeepRuinGenerator.RUINS.add(stoneRoom);
        DeepRuinGenerator.RUINS.add(stoneRoom);
    }
}
