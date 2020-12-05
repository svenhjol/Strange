package svenhjol.strange.base;

import net.minecraft.util.Identifier;
import svenhjol.charm.base.helper.LootHelper;
import svenhjol.strange.Strange;

public class StrangeLoot {
    public static Identifier ANCIENT_RUBBLE = new Identifier(Strange.MOD_ID, "gameplay/ancient_rubble");
    public static Identifier RUIN_RARE = new Identifier(Strange.MOD_ID, "gameplay/ruin_rare");
    public static Identifier VILLAGE_SCROLLKEEPER = new Identifier(Strange.MOD_ID, "gameplay/village_scrollkeeper");

    public static void init() {
        LootHelper.CUSTOM_LOOT_TABLES.add(VILLAGE_SCROLLKEEPER);
    }
}
