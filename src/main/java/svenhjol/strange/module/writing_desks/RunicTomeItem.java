package svenhjol.strange.module.writing_desks;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.knowledge.KnowledgeBranch;

import java.util.Optional;

public class RunicTomeItem extends CharmItem {
    private static final String TAG_RUNES = "Runes";
    private static final String TAG_BRANCH = "Branch";
    private static final String TAG_AUTHOR = "Author";
    private static final String TAG_ITEM = "Item";

    public RunicTomeItem(CharmModule module) {
        super(module, "runic_tome", new FabricItemSettings()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(16));
    }

    public static Optional<String> getRunes(ItemStack tome) {
        return Optional.ofNullable(tome.getOrCreateTag().getString(TAG_RUNES));
    }

    public static Optional<KnowledgeBranch<?, ?>> getBranch(ItemStack tome) {
        return KnowledgeBranch.getByName(tome.getOrCreateTag().getString(TAG_BRANCH));
    }

    public static Optional<String> getAuthor(ItemStack tome) {
        return Optional.ofNullable(tome.getOrCreateTag().getString(TAG_AUTHOR));
    }

    public static Optional<Item> getItem(ItemStack tome) {
        return Registry.ITEM.getOptional(new ResourceLocation(tome.getOrCreateTag().getString(TAG_ITEM)));
    }

    public static void setRunes(ItemStack tome, String runes) {
        tome.getOrCreateTag().putString(TAG_RUNES, runes);

        KnowledgeBranch.getByStartRune(runes.charAt(0)).ifPresent(branch -> {
            tome.getOrCreateTag().putString(TAG_BRANCH, branch.getBranchName());
        });
    }

    public static void setAuthor(ItemStack tome, String author) {
        tome.getOrCreateTag().putString(TAG_AUTHOR, author);
    }

    public static void setItem(ItemStack tome, Item item) {
        ResourceLocation key = Registry.ITEM.getKey(item);
        tome.getOrCreateTag().putString(TAG_ITEM, key.toString());
    }
}
