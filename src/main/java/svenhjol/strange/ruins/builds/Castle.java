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

    //    addStart("start_trap", 1);
        addStart("start_tower", 1);
        addStart("start_tower2", 1);


        ROOMS.put("room_secret", 1);
        ROOMS.put("room_smlib", 2);
        ROOMS.put("room_tower", 3);
        ROOMS.put("room_corner", 3);
        ROOMS.put("room_big1", 3);
        ROOMS.put("room_trap", 2);
    //    ROOMS.put("room_zombies", 1);

    //    CORRIDORS.put("corridor_start1", 3);
    //    CORRIDORS.put("corridor_start2", 2);
    //    CORRIDORS.put("corridor_start3", 2);
        CORRIDORS.put("corridor_stairs", 2);
        CORRIDORS.put("corridor_long", 3);
    //    CORRIDORS.put("corridor_long_high", 2);
        CORRIDORS.put("corridor_med", 2);
        CORRIDORS.put("corridor_med_low", 3);
        CORRIDORS.put("corridor_spawner", 3);
    //    CORRIDORS.put("corridor_spawner_high", 3);

        ENDS.put("end_1", 3);
        ENDS.put("end_2", 3);
        ENDS.put("end_spawner", 3);
        ENDS.put("end_deco", 2);
        ENDS.put("end_deco_high", 2);
        ENDS.put("end_fire_high", 2);
    //    ENDS.put("end_wall3", 1);
    //    ENDS.put("end_wall4", 1);

        registerPool("rooms", ROOMS);
        registerPool("corridors", CORRIDORS);
        registerPool("ends", ENDS);
    }
}
