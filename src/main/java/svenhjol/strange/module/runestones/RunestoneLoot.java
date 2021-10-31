package svenhjol.strange.module.runestones;

import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.Strange;

public class RunestoneLoot {
    // must be a literal path so that we can load the json manually
    public static ResourceLocation OVERWORLD_ITEMS = new ResourceLocation(Strange.MOD_ID, "loot_tables/runestones/overworld_items.json");
    public static ResourceLocation NETHER_ITEMS = new ResourceLocation(Strange.MOD_ID, "loot_tables/runestones/nether_items.json");
    public static ResourceLocation END_ITEMS = new ResourceLocation(Strange.MOD_ID, "loot_tables/runestones/end_items.json");
}
