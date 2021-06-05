package svenhjol.strange.init;

import net.minecraft.util.Identifier;
import svenhjol.charm.helper.LootHelper;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;

public class StrangeLoot {
    public static List<Identifier> REGISTER = new ArrayList<>();

    public static Identifier ROGUELIKE_BTEAM = createLootTable("gameplay/roguelike_bteam");
    public static Identifier END_RUINS = createLootTable("gameplay/end_ruins");
    public static Identifier NETHER_RUINS = createLootTable("gameplay/nether_ruins");
    public static Identifier OVERWORLD_RUINS_COMMON = createLootTable("gameplay/overworld_ruins_common");
    public static Identifier OVERWORLD_RUINS_UNCOMMON = createLootTable("gameplay/overworld_ruins_uncommon");
    public static Identifier OVERWORLD_RUINS_RARE = createLootTable("gameplay/overworld_ruins_rare");
    public static Identifier OVERWORLD_RUINS_EPIC = createLootTable("gameplay/overworld_ruins_epic");
    public static Identifier RUBBLE = createLootTable("gameplay/rubble");
    public static Identifier STONE_CIRCLE = createLootTable("gameplay/stone_circle");
    public static Identifier VILLAGE_SCROLLKEEPER = createLootTable("gameplay/village_scrollkeeper");

    public static void init() {
        LootHelper.CUSTOM_LOOT_TABLES.addAll(REGISTER);
    }

    public static Identifier createLootTable(String name) {
        Identifier id = new Identifier(Strange.MOD_ID, name);
        REGISTER.add(id);
        return id;
    }
}
