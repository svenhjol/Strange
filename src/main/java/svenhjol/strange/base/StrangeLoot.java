package svenhjol.strange.base;

import net.minecraft.util.ResourceLocation;
import svenhjol.strange.Strange;

public class StrangeLoot
{
    public static final ResourceLocation CHESTS_VAULT_BOOKSHELVES = createLootTable("chests/vaults_bookshelves");
    public static final ResourceLocation CHESTS_VAULT_STORAGE = createLootTable("chests/vaults_storage");
    public static final ResourceLocation CHESTS_VAULT_TREASURE = createLootTable("chests/vaults_treasure");
    public static final ResourceLocation STONE_CIRCLE_TREASURE = createLootTable("chests/stone_circle_treasure");

    public static ResourceLocation createLootTable(String path)
    {
        return new ResourceLocation(Strange.MOD_ID, path);
    }
}
