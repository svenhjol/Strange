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

        // TODO: issues with ruin

        // rooms:
        // level1 anvil room is flooded
        // level2 piece that has blue concrete marker
        // level4 creeper room should be an end piece
        // level4 creeper room showing tnt at surface

        // general:
        // mob health >20 doesn't seem to be supported
        // entity armor flags?
        // pull shipwreck loot tables for ruins
        // reduce weight of rare chests

        addStart("start1", 1);

        ROOMS.put("level1_room_cake1", 5);
        ROOMS.put("level1_room_cake2", 5);
        ROOMS.put("level1_room_fires1", 5);
        ROOMS.put("level1_room_fires2", 5);
        ROOMS.put("level1_room_light", 5);
        ROOMS.put("level1_room_spawner1", 5);
        ROOMS.put("level1_room_spawner2", 5);
        ROOMS.put("level1_room_spawner3", 5);
        ROOMS.put("level1_room_storage1", 5);
        ROOMS.put("level1_room_storage2", 5);
        ROOMS.put("level1_room_tiny1", 5);
        ROOMS.put("level1_room_tiny2", 5);
        ROOMS.put("level2_room_dining", 5);
        ROOMS.put("level2_room_droptrap", 5);
        ROOMS.put("level2_room_music", 5);
        ROOMS.put("level2_room_plants", 5);
        ROOMS.put("level2_room_spawner1", 5);
        ROOMS.put("level3_room_cells", 5);
        ROOMS.put("level3_room_cornertomb", 5);
        ROOMS.put("level3_room_cross", 4);
        ROOMS.put("level3_room_droptrap", 2);
        ROOMS.put("level3_room_fires", 5);
        ROOMS.put("level3_room_gravel", 2);
        ROOMS.put("level3_room_joining", 5);
        ROOMS.put("level3_room_skulls", 5);
        ROOMS.put("level3_room_tombs", 5);
        ROOMS.put("level4_room_cells1", 5);
        ROOMS.put("level4_room_creepers", 4);
        ROOMS.put("level4_room_crypt1", 4);
        ROOMS.put("level4_room_junction", 5);
        ROOMS.put("level4_room_skulls", 4);
        ROOMS.put("level4_room_water1", 5);

        SIDES.put("level1_side_blacksmith", 2);
        SIDES.put("level1_side_den", 5);
        SIDES.put("level1_side_fire", 8);
        SIDES.put("level1_side_plain", 10);
        SIDES.put("level1_side_pumpkin", 6);
        SIDES.put("level2_side_bookshelves", 5);
        SIDES.put("level2_side_bteam", 1);
        SIDES.put("level2_side_chest", 5);
        SIDES.put("level2_side_plain", 10);
        SIDES.put("level3_side_irondoor1", 5);
        SIDES.put("level3_side_irondoor2", 5);
        SIDES.put("level3_side_plain", 7);

        CORRIDORS.put("level1_corridor_variant1", 5);
        CORRIDORS.put("level1_corridor_variant2", 5);
        CORRIDORS.put("level1_corridor_variant3", 5);
        CORRIDORS.put("level1_corridor_variant4", 5);
        CORRIDORS.put("level2_corridor_variant1", 5);
        CORRIDORS.put("level2_corridor_variant2", 5);
        CORRIDORS.put("level2_corridor_variant3", 5);
        CORRIDORS.put("level3_corridor_spiders", 2);
        CORRIDORS.put("level3_corridor_tiny", 3);
        CORRIDORS.put("level3_corridor_variant1", 5);
        CORRIDORS.put("level3_corridor_variant2", 5);
        CORRIDORS.put("level4_corridor_variant1", 4);
        CORRIDORS.put("level4_corridor_variant2", 4);
        CORRIDORS.put("level4_corridor_variant3", 4);
        CORRIDORS.put("level4_corridor_variant4", 5);
        CORRIDORS.put("level4_corridor_variant5", 5);
        CORRIDORS.put("level4_corridor_variant6", 5);

        ENDS.put("level1_end1", 5);
        ENDS.put("level2_end1", 5);
        ENDS.put("level3_end1", 5);
        ENDS.put("level3_end2", 5);
        ENDS.put("level3_end3", 5);
        ENDS.put("level4_end1", 4);
        ENDS.put("level4_end2", 4);
        ENDS.put("level4_end3", 4);
        ENDS.put("level4_end_water1", 5);

        registerPool("rooms", ROOMS);
        registerPool("sides", SIDES);
        registerPool("corridors", CORRIDORS);
        registerPool("ends", ENDS);
    }
}
