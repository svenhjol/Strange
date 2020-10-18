package svenhjol.strange.structure.ancientruin;

import svenhjol.strange.Strange;
import svenhjol.strange.structure.BasePiece;

public class StoneRoomRuin extends BasePiece {
    public StoneRoomRuin() {
        super(Strange.MOD_ID, "ancient_ruins", "stone_room");

        addStart("start1", 1);
    }
}
