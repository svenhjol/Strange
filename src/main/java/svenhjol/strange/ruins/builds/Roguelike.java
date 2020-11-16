package svenhjol.strange.ruins.builds;

import svenhjol.charm.base.structure.BaseStructure;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class Roguelike extends BaseStructure {
    private final Map<String, Integer> ROOMS = new HashMap<>();
    private final Map<String, Integer> CORRIDORS = new HashMap<>();
    private final Map<String, Integer> SURFACES = new HashMap<>();
    private final Map<String, Integer> SIDES = new HashMap<>();
    private final Map<String, Integer> ENDS = new HashMap<>();

    public Roguelike() {
        super(Strange.MOD_ID, "ruins", "roguelike");

        addStart("start1", 1);

        SURFACES.put("surface_variant1", 1);

        ROOMS.put("room_1_cake", 1);
        ROOMS.put("room_1_fires", 1);
        ROOMS.put("room_1_light", 1);
        ROOMS.put("room_1_tiny", 1);
        ROOMS.put("room_2_dining", 2);
        ROOMS.put("room_2_droptrap", 2);
        ROOMS.put("room_2_music", 2);
        ROOMS.put("room_2_plants", 2);
        ROOMS.put("room_3_cells", 3);
        ROOMS.put("room_3_cornertomb", 3);
        ROOMS.put("room_3_cross", 3);
        ROOMS.put("room_3_droptrap", 3);
        ROOMS.put("room_3_fires", 3);
        ROOMS.put("room_3_gravel", 3);
        ROOMS.put("room_3_joining", 3);
        ROOMS.put("room_3_skulls", 3);
        ROOMS.put("room_3_tnt", 3);
        ROOMS.put("room_3_tombs", 3);
        ROOMS.put("room_4_water", 4);
        ROOMS.put("stairs_1_2_variant1", 3);
        ROOMS.put("stairs_2_3_variant1", 4);
        ROOMS.put("stairs_3_4_variant1", 5);

        CORRIDORS.put("corridor_1_variant1", 1);
        CORRIDORS.put("corridor_1_variant2", 1);
        CORRIDORS.put("corridor_2_variant1", 1);
        CORRIDORS.put("corridor_2_variant2", 1);
        CORRIDORS.put("corridor_3_spiders", 1);
        CORRIDORS.put("corridor_3_tiny", 1);
        CORRIDORS.put("corridor_3_variant1", 1);
        CORRIDORS.put("corridor_3_variant2", 1);
        CORRIDORS.put("corridor_4_variant1", 1);
        CORRIDORS.put("corridor_4_variant2", 1);

        SIDES.put("side_1_blacksmith", 2);
        SIDES.put("side_1_den", 4);
        SIDES.put("side_1_fire", 8);
        SIDES.put("side_1_plain", 10);
        SIDES.put("side_1_pumpkin", 6);
        SIDES.put("side_2_bookshelves", 6);
        SIDES.put("side_2_bteam", 1);
        SIDES.put("side_2_chest", 4);
        SIDES.put("side_2_plain", 10);
        SIDES.put("side_3_irondoor", 1);

        ENDS.put("end_1_spawner", 1);
        ENDS.put("end_1_variant1", 1);
        ENDS.put("end_3_variant1", 1);

        registerPool("surfaces", SURFACES);
        registerPool("rooms", ROOMS);
        registerPool("corridors", CORRIDORS);
        registerPool("sides", SIDES);
        registerPool("ends", ENDS);
    }
}
