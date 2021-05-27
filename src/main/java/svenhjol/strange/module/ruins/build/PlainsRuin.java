package svenhjol.strange.module.ruins.build;

import svenhjol.charm.world.CharmStructure;
import svenhjol.strange.Strange;

public class PlainsRuin extends CharmStructure {

    public PlainsRuin() {
        super(Strange.MOD_ID, "ruins", "plains");

        addStart("start1", 1);
    }
}

