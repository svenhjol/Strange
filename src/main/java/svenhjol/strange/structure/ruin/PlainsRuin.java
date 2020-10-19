package svenhjol.strange.structure.ruin;

import svenhjol.charm.base.structure.BaseStructure;
import svenhjol.strange.Strange;

public class PlainsRuin extends BaseStructure {

    public PlainsRuin() {
        super(Strange.MOD_ID, "ruins", "plains");

        addStart("start1", 1);
    }
}

