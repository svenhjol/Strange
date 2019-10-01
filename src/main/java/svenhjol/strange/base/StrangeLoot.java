package svenhjol.strange.base;

import net.minecraft.util.ResourceLocation;
import svenhjol.strange.Strange;

public class StrangeLoot
{
    public static final ResourceLocation CHESTS_VAULT_BOOKSHELVES;
    public static final ResourceLocation CHESTS_VAULT_STORAGE;
    public static final ResourceLocation CHESTS_VAULT_TREASURE;

    static {
        CHESTS_VAULT_BOOKSHELVES = new ResourceLocation(Strange.MOD_ID, "chests/vaults_bookshelves");
        CHESTS_VAULT_STORAGE = new ResourceLocation(Strange.MOD_ID, "chests/vaults_storage");
        CHESTS_VAULT_TREASURE = new ResourceLocation(Strange.MOD_ID, "chests/vaults_treasure");
    }
}
