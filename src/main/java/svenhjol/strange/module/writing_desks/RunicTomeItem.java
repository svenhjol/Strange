package svenhjol.strange.module.writing_desks;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.knowledge.KnowledgeBranch;

public class RunicTomeItem extends CharmItem {
    private static final String TAG_RUNES = "Runes";
    private static final String TAG_BRANCH = "Branch";
    private static final String TAG_AUTHOR = "Author";

    public RunicTomeItem(CharmModule module) {
        super(module, "runic_tome", new FabricItemSettings()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(16));
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
}
