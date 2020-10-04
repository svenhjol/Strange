package svenhjol.strange.ruin;

import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class ForestRuin extends BaseRuin {
    private final Map<String, Integer> ROOMS = new HashMap<>();
    private final Map<String, Integer> CORRIDORS = new HashMap<>();
  //  private final Map<String, Integer> ENDS = new HashMap<>();

    public ForestRuin() {
        super(Strange.MOD_ID, "forest_ruin");

        addStart("start1", 1);

       ROOMS.put("room1", 1);
       ROOMS.put("room2", 1);
    ///    ROOMS.put("room_corner2", 1);
    ///    ROOMS.put("room_derp1", 1);
    ///    ROOMS.put("room_skeletons", 1)
    //   ROOMS.put("room_trap", 1);
    //    ROOMS.put("room_zombies", 1);

        CORRIDORS.put("corridor", 1);
    //   CORRIDORS.put("corridor_shattered", 1);
    //    CORRIDORS.put("corridor_small", 1);
    //    CORRIDORS.put("corridor_spawner", 1);
    //    CORRIDORS.put("corridor_tiny", 1);

   //     ENDS.put("end_chest", 1);
   //     ENDS.put("end_erosion", 1);
   //     ENDS.put("end_potions", 1);
    //    ENDS.put("end_wall1", 1);
    //    ENDS.put("end_wall2", 1);
    //    ENDS.put("end_wall3", 1);
    //    ENDS.put("end_wall4", 1);

        registerPool("rooms", ROOMS);
        registerPool("corridors", CORRIDORS);
    //    registerPool("ends", ENDS);
    }
}
