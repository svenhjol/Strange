package svenhjol.strange.ruins.builds;

import svenhjol.charm.base.structure.BaseStructure;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class Roguelike extends BaseStructure {
    private final Map<String, Integer> ROOMS = new HashMap<>();
    private final Map<String, Integer> CORRIDORS = new HashMap<>();
    private final Map<String, Integer> SIDES = new HashMap<>();
    private final Map<String, Integer> ENDS = new HashMap<>();

    public Roguelike() {
        super(Strange.MOD_ID, "ruins", "roguelike");

        addStart("start1", 1);

        ROOMS.put("room_3_cells", 1);
        ROOMS.put("room_3_cornertomb", 1);
        ROOMS.put("room_3_cross", 1);
        ROOMS.put("room_3_droptrap", 1);
        ROOMS.put("room_3_fires", 1);
        ROOMS.put("room_3_gravel", 1);
        ROOMS.put("room_3_joining", 1);
        ROOMS.put("room_3_skulls", 1);
        ROOMS.put("room_3_tnt", 1);
        ROOMS.put("room_3_tombs", 1);
        ROOMS.put("room_4_water", 1);
        ROOMS.put("stairs_2_3_variant1", 1);

        CORRIDORS.put("corridor_3_spiders", 1);
        CORRIDORS.put("corridor_3_tiny", 1);
        CORRIDORS.put("corridor_3_variant1", 1);
        CORRIDORS.put("corridor_3_variant2", 1);
        CORRIDORS.put("corridor_4_variant1", 1);
        CORRIDORS.put("corridor_4_variant2", 1);

        SIDES.put("side_3_irondoor", 1);

        ENDS.put("end_3_variant1", 1);

        registerPool("rooms", ROOMS);
        registerPool("corridors", CORRIDORS);
        registerPool("sides", SIDES);
        registerPool("ends", ENDS);
    }
}
