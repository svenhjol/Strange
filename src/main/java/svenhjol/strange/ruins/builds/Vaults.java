package svenhjol.strange.ruins.builds;

import svenhjol.charm.base.structure.BaseStructure;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class Vaults extends BaseStructure {
    private final Map<String, Integer> ROOMS = new HashMap<>();
    private final Map<String, Integer> CORRIDORS = new HashMap<>();
    private final Map<String, Integer> ENDS = new HashMap<>();

    public Vaults() {
        super(Strange.MOD_ID, "ruins", "vaults");

        addStart("start1", 1);
        addStart("start2", 1);

        ROOMS.put("room_big_staircase", 2);
        ROOMS.put("room_small_dungeon1", 2);
        ROOMS.put("room_small_dungeon2", 2);
        ROOMS.put("room_small_lava", 1);
        ROOMS.put("room_stone_corner", 4);
        ROOMS.put("room_stone_junction", 4);
        ROOMS.put("room_stone_pillars", 4);
        ROOMS.put("room_stone_storage", 3);
        ROOMS.put("room_tall_gangway", 3);
        ROOMS.put("room_tall_ladder", 3);
        ROOMS.put("room_wood_booknook", 3);
        ROOMS.put("room_wood_drop", 4);
        ROOMS.put("room_wood_evoker", 2);

        CORRIDORS.put("corridor_stairs", 1);
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
