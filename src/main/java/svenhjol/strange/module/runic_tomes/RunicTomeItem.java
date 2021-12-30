package svenhjol.strange.module.runic_tomes;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.runes.RuneBranch;
import svenhjol.strange.module.runes.RuneHelper;

@SuppressWarnings("unused")
public class RunicTomeItem extends CharmItem {
    private static final String TAG_RUNES = "Runes";
    private static final String TAG_BRANCH = "Branch";
    private static final String TAG_AUTHOR = "Author";

    public static final Component DEFAULT_NAME;

    public RunicTomeItem(CharmModule module) {
        super(module, "runic_tome", new FabricItemSettings()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(16));
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        // no thanks
    }

    public static ItemStack create(String runes) {
        return create(runes, null);
    }

    public static ItemStack create(String runes, @Nullable Player player) {
        ItemStack tome = new ItemStack(RunicTomes.RUNIC_TOME);
        setRunes(tome, runes);

        if (player != null) {
            setAuthor(tome, player.getName().getString());
        }

        RuneBranch<?, ?> branch = RuneHelper.branch(runes);
        if (branch != null) {

            // Set the branch name of this tome. Helps with item icon coloring.
            tome.getOrCreateTag().putString(TAG_BRANCH, branch.getBranchName());

            // Try and fetch the nice name of the location this tome points to.
            // If found, set it as the title of the tome.
            setBranch(tome, branch.getBranchName());
            String prettyName = branch.getValueName(runes);

            if (prettyName != null) {
                tome.setHoverName(new TextComponent(prettyName));
            }
        }

        if (!tome.hasCustomHoverName()) {
            tome.setHoverName(DEFAULT_NAME);
        }

        return tome;
    }

    public static String getRunes(ItemStack tome) {
        return tome.getOrCreateTag().getString(TAG_RUNES);
    }

    public static String getBranch(ItemStack tome) {
        return tome.getOrCreateTag().getString(TAG_BRANCH);
    }

    public static String getAuthor(ItemStack tome) {
        return tome.getOrCreateTag().getString(TAG_AUTHOR);
    }

    public static void setRunes(ItemStack tome, String runes) {
        tome.getOrCreateTag().putString(TAG_RUNES, runes);
    }

    public static void setAuthor(ItemStack tome, String author) {
        tome.getOrCreateTag().putString(TAG_AUTHOR, author);
    }

    public static void setBranch(ItemStack tome, String branch) {
        tome.getOrCreateTag().putString(TAG_BRANCH, branch);
    }

    static {
        DEFAULT_NAME = new TranslatableComponent("gui.strange.writing_desks.runic_tome");
    }
}
