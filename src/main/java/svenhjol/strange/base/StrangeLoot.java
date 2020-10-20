package svenhjol.strange.base;

import net.minecraft.util.Identifier;
import svenhjol.charm.base.helper.LootHelper;
import svenhjol.strange.Strange;

public class StrangeLoot {
    public static Identifier ANCIENT_RUBBLE = new Identifier(Strange.MOD_ID, "gameplay/ancient_rubble");
    public static Identifier FOUNDATIONS = new Identifier(Strange.MOD_ID, "gameplay/foundations");

    public static void init() {
        LootHelper.CUSTOM_LOOT_TABLES.add(ANCIENT_RUBBLE);
        LootHelper.CUSTOM_LOOT_TABLES.add(FOUNDATIONS);
    }
}
