package svenhjol.strange.module.ruins.builds;

import svenhjol.charm.world.CharmStructure;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class Vaults extends CharmStructure {
    private final Map<String, Integer> ROOMS = new HashMap<>();
    private final Map<String, Integer> CORRIDORS = new HashMap<>();
    private final Map<String, Integer> ENDS = new HashMap<>();

    public Vaults() {
        super(Strange.MOD_ID, "ruins", "vaults");

        addStart("start1", 1);
        addStart("start2", 1);

        ROOMS.put("room_big_crypt", 3);
        ROOMS.put("room_big_dungeon", 2);
        ROOMS.put("room_big_forge", 2);
        ROOMS.put("room_big_library", 1);
        ROOMS.put("room_big_portal", 1);
        ROOMS.put("room_big_runes", 2);
        ROOMS.put("room_big_staircase", 3);
        ROOMS.put("room_big_waterfall", 3);
        ROOMS.put("room_small_dungeon1", 4);
        ROOMS.put("room_small_dungeon2", 4);
        ROOMS.put("room_small_lava", 4);
        ROOMS.put("room_stone_corner", 5);
        ROOMS.put("room_stone_junction", 5);
        ROOMS.put("room_stone_pillars", 5);
        ROOMS.put("room_stone_storage", 5);
        ROOMS.put("room_tall_gangway1", 4);
        ROOMS.put("room_tall_gangway2", 4);
        ROOMS.put("room_tall_ladder1", 4);
        ROOMS.put("room_tall_ladder2", 4);
        ROOMS.put("room_wood_booknook", 4);
        ROOMS.put("room_wood_drop", 4);
        ROOMS.put("room_wood_evoker", 4);

        CORRIDORS.put("corridor_stairs", 2);
        CORRIDORS.put("corridor_stone_arches", 2);
        CORRIDORS.put("corridor_stone_broken1", 2);
        CORRIDORS.put("corridor_stone_broken2", 2);
        CORRIDORS.put("corridor_stone_corner1", 2);
        CORRIDORS.put("corridor_stone_corner2", 2);
        CORRIDORS.put("corridor_stone_gap", 2);
        CORRIDORS.put("corridor_wood_corner1", 2);
        CORRIDORS.put("corridor_wood_corner2", 2);
        CORRIDORS.put("corridor_wood_gap", 2);
        CORRIDORS.put("corridor_wood_meeting", 1);
        CORRIDORS.put("corridor_wood_plain", 2);
        CORRIDORS.put("corridor_wood_windows", 2);

        ENDS.put("end_stone_alcove1", 2);
        ENDS.put("end_stone_alcove2", 2);
        ENDS.put("end_stone_books", 1);
        ENDS.put("end_stone_nook", 2);
        ENDS.put("end_stone_storage", 1);
        ENDS.put("end_wood_alcove1", 2);
        ENDS.put("end_wood_alcove2", 2);
        ENDS.put("end_wood_alcove3", 2);
        ENDS.put("end_wood_books", 1);
        ENDS.put("end_wood_chests", 1);

        registerPool("rooms", ROOMS);
        registerPool("corridors", CORRIDORS);
        registerPool("ends", ENDS);
    }
}
