package svenhjol.strange.foundation;

import svenhjol.charm.base.structure.BaseStructure;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class CatacombsFoundation extends BaseStructure {
    private final Map<String, Integer> ROOMS = new HashMap<>();
    private final Map<String, Integer> ENDS = new HashMap<>();

    public CatacombsFoundation() {
        super(Strange.MOD_ID, "foundations", "catacombs");

        addStart("start1", 1);
        addStart("start2", 1);

        // add the ends as rooms too, whynot
        ROOMS.put("room_dirt", 1);
        ROOMS.put("room_filled1", 3);
        ROOMS.put("room_filled2", 3);
        ROOMS.put("room_gap", 1);
        ROOMS.put("room_skulls", 1);
        ROOMS.put("room_tall", 2);
        ROOMS.put("end1", 3);
        ROOMS.put("end2", 3);
        ROOMS.put("end3", 3);
        ROOMS.put("end4", 3);

        ENDS.put("end1", 1);
        ENDS.put("end2", 1);
        ENDS.put("end3", 1);
        ENDS.put("end4", 1);

        registerPool("rooms", ROOMS);
        registerPool("ends", ENDS);
    }
}
