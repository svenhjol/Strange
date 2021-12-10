package svenhjol.strange.module.overworld_ruins.build;

import svenhjol.strange.Strange;
import svenhjol.strange.init.StrangeStructures;
import svenhjol.strange.structure.StrangeStructure;

public class TestRuin extends StrangeStructure {
    public TestRuin() {
        super(Strange.MOD_ID, "ruins", "test");
        addStart("test_start1", 1, StrangeStructures.DEFAULT_PROCESSORS);
    }
}
