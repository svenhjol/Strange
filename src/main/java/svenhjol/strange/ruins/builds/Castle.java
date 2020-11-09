package svenhjol.strange.ruins.builds;

import svenhjol.charm.base.structure.BaseStructure;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class Castle extends BaseStructure {
    private final Map<String, Integer> ROOMS = new HashMap<>();
    private final Map<String, Integer> CORRIDORS = new HashMap<>();
    private final Map<String, Integer> ENDS = new HashMap<>();

    public Castle() {
        super(Strange.MOD_ID, "ruins", "castle");

        addStart("start_nature", 1);
        addStart("start_trap", 1);
        addStart("start_tower", 1);
        addStart("start_tower2", 1);


        ROOMS.put("room_secret", 3);
        ROOMS.put("room_smlib", 3);
        ROOMS.put("room_tower", 1);
    //    ROOMS.put("room_derp1", 1);
    //    ROOMS.put("room_skeletons", 1);
    //    ROOMS.put("room_trap", 1);
    //    ROOMS.put("room_zombies", 1);

        CORRIDORS.put("corridor_start1", 3);
        CORRIDORS.put("corridor_start2", 2);
        CORRIDORS.put("corridor_start3", 2);
        CORRIDORS.put("corridor_stairs", 2);
        CORRIDORS.put("corridor_stairs_stone", 2);
    //    CORRIDORS.put("corridor_small3", 3);

        ENDS.put("end1", 2);
        ENDS.put("end_spawner", 2);
    //    ENDS.put("end_potions", 1);
    //    ENDS.put("end_wall1", 1);
    //    ENDS.put("end_wall2", 1);
    //    ENDS.put("end_wall3", 1);
    //    ENDS.put("end_wall4", 1);

        registerPool("rooms", ROOMS);
        registerPool("corridors", CORRIDORS);
        registerPool("ends", ENDS);
    }
}
