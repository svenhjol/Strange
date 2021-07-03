package svenhjol.strange.module.ruins.builds;

import svenhjol.charm.world.CharmStructure;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class Castle extends CharmStructure {
    private final Map<String, Integer> ROOMS = new HashMap<>();
    private final Map<String, Integer> CORRIDORS = new HashMap<>();
    private final Map<String, Integer> ENDS = new HashMap<>();

    public Castle() {
        super(Strange.MOD_ID, "ruins", "castle");

        addStart("start_tower", 1);
        addStart("start_tower2", 1);

        ROOMS.put("room_secret", 2);
        ROOMS.put("room_smlib", 1);
        ROOMS.put("room_tower", 2);
        ROOMS.put("room_corner_upper", 3);
        ROOMS.put("room_corner_lower", 3);
        ROOMS.put("room_wood", 2);
        ROOMS.put("room_trap", 1);
        ROOMS.put("room_nature", 2);
        ROOMS.put("room_damaged", 2);

        CORRIDORS.put("corridor_long", 3);
        CORRIDORS.put("corridor_stairs", 3);
        CORRIDORS.put("corridor_long_lower", 3);
        CORRIDORS.put("corridor_med", 2);
        CORRIDORS.put("corridor_med_low", 3);
        CORRIDORS.put("corridor_spawner_lower", 3);
        CORRIDORS.put("corridor_spawner_upper", 3);

        ENDS.put("end1", 3);
        ENDS.put("end2", 3);
        ENDS.put("end_spawner", 3);
        ENDS.put("end_deco", 2);
        ENDS.put("end_deco_high", 2);
        ENDS.put("end_fire_high", 2);

        registerPool("rooms", ROOMS);
        registerPool("corridors", CORRIDORS);
        registerPool("ends", ENDS);
    }
}
