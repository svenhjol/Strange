package svenhjol.strange.ruins.builds;

import svenhjol.charm.base.structure.BaseStructure;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class Roguelike extends BaseStructure {
    private final Map<String, Integer> CORRIDORS = new HashMap<>();
    private final Map<String, Integer> ENDS = new HashMap<>();
    private final Map<String, Integer> ROOMS = new HashMap<>();
    private final Map<String, Integer> SIDES = new HashMap<>();

    public Roguelike() {
        super(Strange.MOD_ID, "ruins", "roguelike");

        addStart("start1", 1);

        ROOMS.put("level1_room_cake1", 1);
        ROOMS.put("level1_room_cake2", 1);
        ROOMS.put("level1_room_fires1", 1);
        ROOMS.put("level1_room_fires2", 1);
        ROOMS.put("level1_room_light", 1);
        ROOMS.put("level1_room_spawner1", 1);
        ROOMS.put("level1_room_spawner2", 1);
        ROOMS.put("level1_room_spawner3", 1);
        ROOMS.put("level1_room_storage1", 1);
        ROOMS.put("level1_room_storage2", 1);
        ROOMS.put("level1_room_tiny1", 1);
        ROOMS.put("level1_room_tiny2", 1);

        SIDES.put("level1_side_blacksmith", 2);
        SIDES.put("level1_side_den", 5);
        SIDES.put("level1_side_fire", 8);
        SIDES.put("level1_side_plain", 10);
        SIDES.put("level1_side_pumpkin", 6);

        CORRIDORS.put("level1_corridor_variant1", 1);
        CORRIDORS.put("level1_corridor_variant2", 1);
        CORRIDORS.put("level1_corridor_variant3", 1);
        CORRIDORS.put("level1_corridor_variant4", 1);

        ENDS.put("level1_end1", 6);

        registerPool("rooms", ROOMS);
        registerPool("sides", SIDES);
        registerPool("corridors", CORRIDORS);
        registerPool("ends", ENDS);
    }
}
