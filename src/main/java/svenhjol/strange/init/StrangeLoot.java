package svenhjol.strange.init;

import svenhjol.charm.helper.LootHelper;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class StrangeLoot {
    public static List<ResourceLocation> REGISTER = new ArrayList<>();

    public static ResourceLocation ROGUELIKE_BTEAM = createLootTable("gameplay/roguelike_bteam");
    public static ResourceLocation END_RUINS = createLootTable("gameplay/end_ruins");
    public static ResourceLocation NETHER_RUINS = createLootTable("gameplay/nether_ruins");
    public static ResourceLocation OVERWORLD_RUINS_COMMON = createLootTable("gameplay/overworld_ruins_common");
    public static ResourceLocation OVERWORLD_RUINS_UNCOMMON = createLootTable("gameplay/overworld_ruins_uncommon");
    public static ResourceLocation OVERWORLD_RUINS_RARE = createLootTable("gameplay/overworld_ruins_rare");
    public static ResourceLocation OVERWORLD_RUINS_EPIC = createLootTable("gameplay/overworld_ruins_epic");
    public static ResourceLocation RUBBLE = createLootTable("gameplay/rubble");
    public static ResourceLocation STONE_CIRCLE = createLootTable("gameplay/stone_circle");
    public static ResourceLocation VILLAGE_SCROLLKEEPER = createLootTable("gameplay/village_scrollkeeper");

    public static void init() {
        LootHelper.CUSTOM_LOOT_TABLES.addAll(REGISTER);
    }

    public static ResourceLocation createLootTable(String name) {
        ResourceLocation id = new ResourceLocation(Strange.MOD_ID, name);
        REGISTER.add(id);
        return id;
    }
}
