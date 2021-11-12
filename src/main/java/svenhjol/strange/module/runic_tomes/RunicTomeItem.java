package svenhjol.strange.module.runic_tomes;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.knowledge.KnowledgeBranch;

import java.util.Optional;

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

    public static ItemStack create(Player player, String runes) {
        ItemStack tome = new ItemStack(RunicTomes.RUNIC_TOME);
        setRunes(tome, runes);
        setAuthor(tome, player.getName().getString());

        KnowledgeBranch.getByStartRune(runes.charAt(0))
            .flatMap(branch -> branch.getPrettyName(runes)).ifPresent(name -> tome.setHoverName(new TextComponent(name)));

        if (!tome.hasCustomHoverName()) {
            tome.setHoverName(DEFAULT_NAME);
        }

        return tome;
    }

    public static Optional<String> getRunes(ItemStack tome) {
        return Optional.ofNullable(tome.getOrCreateTag().getString(TAG_RUNES));
    }

    public static Optional<String> getAuthor(ItemStack tome) {
        return Optional.ofNullable(tome.getOrCreateTag().getString(TAG_AUTHOR));
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

    static {
        DEFAULT_NAME = new TranslatableComponent("gui.strange.writing_desks.runic_tome");
    }
}
