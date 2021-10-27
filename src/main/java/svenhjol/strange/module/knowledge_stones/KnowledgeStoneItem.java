package svenhjol.strange.module.knowledge_stones;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import svenhjol.charm.helper.StringHelper;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.KnowledgeData;

import java.util.*;
import java.util.function.Consumer;

public class KnowledgeStoneItem extends CharmItem {
    public static final String RARITY_TAG = "Rarity";

    public KnowledgeStoneItem(CharmModule module) {
        super(module, "knowledge_stone", new FabricItemSettings()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(64));
    }

    @Override
    public boolean isFoil(ItemStack stone) {
        Rarity rarity = getKnowledgeRarity(stone);
        return rarity == Rarity.RARE || rarity == Rarity.EPIC;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        JournalData journal = Journals.getJournalData(player).orElseThrow();
        KnowledgeData knowledge = Knowledge.getSavedData().orElseThrow();
        Rarity rarity = getKnowledgeRarity(stack);

        if (!level.isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            MinecraftServer server = serverPlayer.getServer();
            if (server == null) {
                return InteractionResultHolder.fail(stack);
            }

            boolean learnedThing = switch (rarity) {
                case COMMON -> tryLearnFromBranch(journal.getLearnedBiomes(), knowledge.biomes, b -> {
                    journal.learnBiome(b);
                    clientMessage(serverPlayer, "biome", b.getPath());
                });

                case UNCOMMON -> tryLearnFromBranch(journal.getLearnedStructures(), knowledge.structures, s -> {
                    journal.learnStructure(s);
                    clientMessage(serverPlayer, "structure", s.getPath());
                });

                case RARE -> tryLearnFromBranch(journal.getLearnedPlayers(), knowledge.players, p -> {
                    journal.learnPlayer(p);
                    String playerName;

                    // TODO: move all this to PlayerHelper
                    Optional<ServerPlayer> learnedPlayer = Optional.ofNullable(server.getPlayerList().getPlayer(p));
                    if (learnedPlayer.isPresent()) {
                        playerName = learnedPlayer.get().getName().getString();
                    } else {
                        playerName = p.toString();
                    }

                    clientMessage(serverPlayer, "player", playerName);
                });

                case EPIC -> tryLearnFromBranch(journal.getLearnedDimensions(), knowledge.dimensions, d -> {
                    journal.learnDimension(d);
                    clientMessage(serverPlayer, "dimension", d.getPath());
                });
            };

            if (learnedThing) {
                // TODO: rune glyph effect and sound effect
            } else {
                player.giveExperienceLevels((rarity.ordinal() + 1) * 2);
            }

            stack.shrink(1);
        }

        return InteractionResultHolder.success(stack);
    }

    private <T> boolean tryLearnFromBranch(List<T> playerKnowledge, KnowledgeBranch<?, T> branch, Consumer<T> onLearn) {
        if (playerKnowledge.size() < branch.size()) {
            List<T> list = new ArrayList<>(branch.values());
            Collections.shuffle(list, new Random());
            for (T item : list) {
                if (!playerKnowledge.contains(item)) {
                    onLearn.accept(item);
                    return true;
                }
            }
        }
        return false;
    }

    private void clientMessage(ServerPlayer player, String type, String name) {
        Component component = new TranslatableComponent("gui.strange.knowledge_stones.learned_" + type, StringHelper.snakeToPretty(name, true));
        player.displayClientMessage(component, true);
    }

    public static void setKnowledgeRarity(ItemStack stone, Rarity rarity) {
        stone.getOrCreateTag().putString(RARITY_TAG, rarity.name());
    }

    public static Rarity getKnowledgeRarity(ItemStack stone) {
        Rarity rarity;
        String rarityNbt = stone.getOrCreateTag().getString(RARITY_TAG);

        try {
            rarity = Rarity.valueOf(rarityNbt);
        } catch (Exception e) {
            rarity = Rarity.COMMON;
        }

        return rarity;
    }

    public static ItemStack createWithRarity(Rarity rarity) {
        ItemStack stone = new ItemStack(KnowledgeStones.KNOWLEDGE_STONE);
        setKnowledgeRarity(stone, rarity);
        Component component = new TranslatableComponent("item.strange.knowledge_stone_with_rarity", StringHelper.capitalize(rarity.name().toLowerCase(Locale.ROOT)));
        stone.setHoverName(component);
        return stone;
    }
}
