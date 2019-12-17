package svenhjol.strange.base;

import net.minecraft.util.ResourceLocation;
import svenhjol.meson.helper.LootHelper;
import svenhjol.strange.Strange;

public class StrangeLoot
{
    public static final ResourceLocation CHESTS_VAULT_BOOKSHELVES = createLootTable("chests/vaults/vaults_bookshelf");
    public static final ResourceLocation CHESTS_VAULT_STORAGE = createLootTable("chests/vaults/vaults_storage");
    public static final ResourceLocation CHESTS_VAULT_TREASURE = createLootTable("chests/vaults/vaults_treasure");
    public static final ResourceLocation CHESTS_SPELLS = createLootTable("chests/spells");
    public static final ResourceLocation CHESTS_STONE_CIRCLE_TREASURE = createLootTable("chests/stone_circle_treasure");

    public static ResourceLocation createLootTable(String path)
    {
        ResourceLocation res = new ResourceLocation(Strange.MOD_ID, path);
        LootHelper.customTables.add(res);
        return res;
    }

    public static void init() {}
}
