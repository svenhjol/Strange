package svenhjol.strange.foundation;

import svenhjol.charm.base.structure.BaseStructure;
import svenhjol.strange.Strange;

public class StoneRoomFoundation extends BaseStructure {
    public StoneRoomFoundation() {
        super(Strange.MOD_ID, "foundations", "stone_room");

        addStart("start1", 1);
        addStart("start2", 1);
        addStart("start3", 1);
        addStart("start4", 1);
    }
}
