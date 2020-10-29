package svenhjol.strange.ruin;

import svenhjol.charm.base.structure.BaseStructure;
import svenhjol.strange.Strange;

public class TestRuin extends BaseStructure {
    public TestRuin() {
        super(Strange.MOD_ID, "ruins", "test");
        addStart("test_start1", 1);
    }
}
