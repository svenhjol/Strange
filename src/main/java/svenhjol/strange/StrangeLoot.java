package svenhjol.strange;

import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LootHelper;

import java.util.ArrayList;
import java.util.List;

public class StrangeLoot {
    public static List<ResourceLocation> REGISTER = new ArrayList<>();

    public static void init() {
        LootHelper.CUSTOM_LOOT_TABLES.addAll(REGISTER);
    }

    public static ResourceLocation createLootTable(String name) {
        ResourceLocation id = new ResourceLocation(Strange.MOD_ID, name);
        REGISTER.add(id);
        return id;
    }
}