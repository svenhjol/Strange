package svenhjol.strange.module.ruins.build;

import svenhjol.charm.world.CharmStructure;
import svenhjol.strange.Strange;

public class TestRuin extends CharmStructure {
    public TestRuin() {
        super(Strange.MOD_ID, "ruins", "test");
        addStart("test_start1", 1);
    }
}
