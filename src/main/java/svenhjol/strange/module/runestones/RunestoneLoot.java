package svenhjol.strange.module.runestones;

import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LootHelper;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;

public class RunestoneLoot {
    public static List<ResourceLocation> REGISTER = new ArrayList<>();

    public static ResourceLocation BASIC = createLootTable("runestones/basic");

    public static void create() {
        LootHelper.CUSTOM_LOOT_TABLES.addAll(REGISTER);
    }

    public static ResourceLocation createLootTable(String name) {
        ResourceLocation id = new ResourceLocation(Strange.MOD_ID, name);
        REGISTER.add(id);
        return id;
    }
}
