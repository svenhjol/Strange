package svenhjol.strange.ruin;

import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class PlainsRuin extends BaseRuin {
    private final Map<String, Integer> ROOMS = new HashMap<>();
    private final Map<String, Integer> CORRIDORS = new HashMap<>();
    private final Map<String, Integer> ENDS = new HashMap<>();

    public PlainsRuin() {
        super(Strange.MOD_ID, "plains");

        addStart("start1", 1);

        //ROOMS.put("room1", 1);
        //ROOMS.put("room2", 1);
        //ROOMS.put("room3", 1);
        //ROOMS.put("room4", 1);
        ///    ROOMS.put("room_skeletons", 1)
        //   ROOMS.put("room_trap", 1);
        //    ROOMS.put("room_zombies", 1);

        //CORRIDORS.put("corridor", 1);
        //CORRIDORS.put("corridor1", 1);
       // CORRIDORS.put("corridor2", 1);
        //CORRIDORS.put("corridor3", 1);
        //    CORRIDORS.put("corridor_tiny", 1);

        //ENDS.put("end1", 1);
        //ENDS.put("end2", 1);
        //ENDS.put("end3", 1);
        //    ENDS.put("end_wall1", 1);
        //    ENDS.put("end_wall2", 1);
        //    ENDS.put("end_wall3", 1);
        //    ENDS.put("end_wall4", 1);

        //registerPool("rooms", ROOMS);
        //registerPool("corridors", CORRIDORS);
        //registerPool("ends", ENDS);
    }
}

