package svenhjol.strange.ruins.builds;

import svenhjol.charm.base.structure.BaseStructure;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class StoneRoom extends BaseStructure {
    private final Map<String, Integer> CORRIDORS = new HashMap<>();
    private final Map<String, Integer> ROOMS = new HashMap<>();
    private final Map<String, Integer> ENDS = new HashMap<>();

    public StoneRoom() {
        super(Strange.MOD_ID, "ruins", "stone_room");

        addStart("start1", 2);
        addStart("start2", 2);
        addStart("start3", 1);

        // add the ends as corridors too, whynot
        CORRIDORS.put("corridor1", 2);
        CORRIDORS.put("corridor2", 2);
        CORRIDORS.put("corridor3", 3);
        CORRIDORS.put("corridor4", 3);
        CORRIDORS.put("end1", 1);
        CORRIDORS.put("end2", 1);
        CORRIDORS.put("end3", 1);
        CORRIDORS.put("end4", 1);

        // add the ends as rooms too, whynot
        ROOMS.put("room_dirt", 2);
        ROOMS.put("room_filled1", 3);
        ROOMS.put("room_filled2", 3);
        ROOMS.put("room_gap", 2);
        ROOMS.put("room_skulls", 2);
        ROOMS.put("room_tall", 2);
        ROOMS.put("end1", 1);
        ROOMS.put("end2", 1);
        ROOMS.put("end3", 1);
        ROOMS.put("end4", 1);

        ENDS.put("end1", 1);
        ENDS.put("end2", 1);
        ENDS.put("end3", 1);
        ENDS.put("end4", 1);

        registerPool("corridors", CORRIDORS);
        registerPool("rooms", ROOMS);
        registerPool("ends", ENDS);
    }
}
