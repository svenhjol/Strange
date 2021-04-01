package svenhjol.strange.base;

import net.minecraft.util.Identifier;
import svenhjol.charm.base.helper.LootHelper;
import svenhjol.strange.Strange;

public class StrangeLoot {
    public static Identifier END_RUINS = new Identifier(Strange.MOD_ID, "gameplay/end_ruins");
    public static Identifier NETHER_RUINS = new Identifier(Strange.MOD_ID, "gameplay/nether_ruins");
    public static Identifier OVERWORLD_RUINS_COMMON = new Identifier(Strange.MOD_ID, "gameplay/overworld_ruins_common");
    public static Identifier OVERWORLD_RUINS_UNCOMMON = new Identifier(Strange.MOD_ID, "gameplay/overworld_ruins_uncommon");
    public static Identifier OVERWORLD_RUINS_RARE = new Identifier(Strange.MOD_ID, "gameplay/overworld_ruins_rare");
    public static Identifier OVERWORLD_RUINS_EPIC = new Identifier(Strange.MOD_ID, "gameplay/overworld_ruins_epic");
    public static Identifier RUBBLE = new Identifier(Strange.MOD_ID, "gameplay/rubble");
    public static Identifier STONE_CIRCLE = new Identifier(Strange.MOD_ID, "gameplay/stone_circle");
    public static Identifier VILLAGE_SCROLLKEEPER = new Identifier(Strange.MOD_ID, "gameplay/village_scrollkeeper");

    public static void init() {
        LootHelper.CUSTOM_LOOT_TABLES.add(VILLAGE_SCROLLKEEPER);
    }
}
