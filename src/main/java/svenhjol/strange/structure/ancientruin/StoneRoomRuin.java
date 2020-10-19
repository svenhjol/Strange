package svenhjol.strange.structure.ancientruin;

import svenhjol.charm.base.structure.BaseStructure;
import svenhjol.strange.Strange;

public class StoneRoomRuin extends BaseStructure {
    public StoneRoomRuin() {
        super(Strange.MOD_ID, "ancient_ruins", "stone_room");

        addStart("start1", 1);
    }
}
