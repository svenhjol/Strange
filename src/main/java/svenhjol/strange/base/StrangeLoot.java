package svenhjol.strange.base;

import net.minecraft.util.Identifier;
import svenhjol.charm.base.helper.LootHelper;
import svenhjol.strange.Strange;

public class StrangeLoot {
    public static Identifier RUBBLE = new Identifier(Strange.MOD_ID, "gameplay/rubble");
    public static Identifier RUINS_COMMON = new Identifier(Strange.MOD_ID, "gameplay/ruins_common");
    public static Identifier RUINS_UNCOMMON = new Identifier(Strange.MOD_ID, "gameplay/ruins_uncommon");
    public static Identifier RUINS_RARE = new Identifier(Strange.MOD_ID, "gameplay/ruins_rare");
    public static Identifier RUINS_EPIC = new Identifier(Strange.MOD_ID, "gameplay/ruins_epic");
    public static Identifier VILLAGE_SCROLLKEEPER = new Identifier(Strange.MOD_ID, "gameplay/village_scrollkeeper");

    public static void init() {
        LootHelper.CUSTOM_LOOT_TABLES.add(VILLAGE_SCROLLKEEPER);
    }
}
