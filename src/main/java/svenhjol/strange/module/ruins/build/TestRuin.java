package svenhjol.strange.module.ruins.build;

import svenhjol.strange.Strange;
import svenhjol.strange.module.structures.BaseStructure;
import svenhjol.strange.module.structures.Structures;

public class TestRuin extends BaseStructure {
    public TestRuin() {
        super(Strange.MOD_ID, "ruins", "test");
        addStart("test_start1", 1, Structures.DEFAULT_PROCESSORS);
    }
}
