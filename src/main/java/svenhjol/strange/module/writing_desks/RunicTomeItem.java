package svenhjol.strange.module.writing_desks;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.knowledge.Destination;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.branches.DestinationsBranch;
import svenhjol.strange.module.knowledge.branches.SpecialsBranch;
import svenhjol.strange.module.runestones.RunestoneHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class RunicTomeItem extends CharmItem {
    private static final String TAG_RUNES = "Runes";
    private static final String TAG_BRANCH = "Branch";
    private static final String TAG_AUTHOR = "Author";
    private static final String TAG_ITEM = "Item";

    public static final Component DEFAULT_NAME;

    public RunicTomeItem(CharmModule module) {
        super(module, "runic_tome", new FabricItemSettings()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(16));
    }

    public static ItemStack create(Player player, String runes) {
        ItemStack tome = new ItemStack(WritingDesks.RUNIC_TOME);
        setRunes(tome, runes);
        setAuthor(tome, player.getName().getString());

        KnowledgeBranch.getByStartRune(runes.charAt(0)).ifPresent(branch -> {
            List<Item> items = new ArrayList<>();

            if (branch instanceof DestinationsBranch destinations) {
                // destinations have fixed item requirements
                Optional<Destination> optDest = destinations.get(runes);
                if (optDest.isPresent()) {
                    items = optDest.get().items;
                }
            } else if (branch instanceof SpecialsBranch specials) {
                // specials have fixed item requirements
                Optional<Destination> optDest = specials.get(runes);
                if (optDest.isPresent()) {
                    items = optDest.get().items;
                }
            } else {
                // generate item from rune length
                float difficulty = (runes.length() / (float) Knowledge.MAX_LENGTH);
                Random random = new Random((long)(difficulty + runes.hashCode()));
                items = List.of(RunestoneHelper.getItem(DimensionHelper.getDimension(player.level), difficulty, random));
            }

            if (!items.isEmpty()) {
                setItem(tome, items.get(0));
            }

            // set the tome's branch name to the prettyname of the rune branch
            branch.getPrettyName(runes).ifPresent(name -> tome.setHoverName(new TextComponent(name)));
        });

        if (!tome.hasCustomHoverName()) {
            tome.setHoverName(DEFAULT_NAME);
        }

        return tome;
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

    static {
        DEFAULT_NAME = new TranslatableComponent("gui.strange.writing_desks.runic_tome");
    }
}
