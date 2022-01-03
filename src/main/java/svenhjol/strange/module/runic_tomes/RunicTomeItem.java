package svenhjol.strange.module.runic_tomes;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.module.stackable_enchanted_books.StackableEnchantedBooks;
import svenhjol.strange.module.runes.RuneBranch;
import svenhjol.strange.module.runes.RuneHelper;

import java.util.Locale;

@SuppressWarnings("unused")
public class RunicTomeItem extends CharmItem {
    private static final String TAG_RUNES = "Runes";
    private static final String TAG_AUTHOR = "Author";

    public RunicTomeItem(CharmModule module, String branchName) {
        super(module, branchName.toLowerCase(Locale.ROOT) + "_runic_tome", new FabricItemSettings()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(StackableEnchantedBooks.stackSize));
    }

    public static ItemStack create(String runes) {
        return create(runes, null);
    }

    public static ItemStack create(String runes, @Nullable Player player) {
        RuneBranch<?, ?> branch = RuneHelper.branch(runes);

        if (branch == null || !RunicTomes.RUNIC_TOMES.containsKey(branch.getBranchName())) {
            return ItemStack.EMPTY;
        }

        ItemStack tome = new ItemStack(RunicTomes.RUNIC_TOMES.get(branch.getBranchName()));
        setRunes(tome, runes);

        // Try and fetch the nice name of the location this tome points to.
        // If found, set it as the title of the tome.
        String prettyName = branch.getValueName(runes);
        if (prettyName != null) {
            tome.setHoverName(new TextComponent(prettyName));
        }

        if (player != null) {
            setAuthor(tome, player.getName().getString());
        }

        return tome;
    }

    public static String getRunes(ItemStack tome) {
        return tome.getOrCreateTag().getString(TAG_RUNES);
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
}
