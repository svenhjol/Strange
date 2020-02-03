package svenhjol.strange.base;

import net.minecraft.util.ResourceLocation;
import svenhjol.meson.helper.LootHelper;
import svenhjol.strange.Strange;

public class StrangeLoot
{
    public static final ResourceLocation CHESTS_VAULT_BOOKSHELVES;
    public static final ResourceLocation CHESTS_VAULT_STORAGE;
    public static final ResourceLocation CHESTS_VAULT_TREASURE;

    public static ResourceLocation createLootTable(String path)
    {
        ResourceLocation res = new ResourceLocation(Strange.MOD_ID, path);
        LootHelper.customTables.add(res);
        return res;
    }

    public static void init() {}

    static
    {
        CHESTS_VAULT_BOOKSHELVES = createLootTable("chests/vaults/vaults_bookshelf");
        CHESTS_VAULT_STORAGE = createLootTable("chests/vaults/vaults_storage");
        CHESTS_VAULT_TREASURE = createLootTable("chests/vaults/vaults_treasure");
    }
}
