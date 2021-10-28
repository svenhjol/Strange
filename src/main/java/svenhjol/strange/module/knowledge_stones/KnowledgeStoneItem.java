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
import net.minecraft.world.level.Level;
import svenhjol.charm.enums.ICharmEnum;
import svenhjol.charm.helper.PlayerHelper;
import svenhjol.charm.helper.StringHelper;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.KnowledgeData;

import java.util.*;
import java.util.function.Function;

public class KnowledgeStoneItem extends CharmItem {
    private final Type type;

    public KnowledgeStoneItem(CharmModule module, Type type) {
        super(module, type.getSerializedName() + "_knowledge_stone", new FabricItemSettings()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(64));

        this.type = type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        KnowledgeStoneItem stone = (KnowledgeStoneItem)held.getItem();
        JournalData journal = Journals.getJournalData(player).orElseThrow();
        KnowledgeData knowledge = Knowledge.getSavedData().orElseThrow();

        if (!level.isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            MinecraftServer server = serverPlayer.getServer();
            if (server == null) {
                return InteractionResultHolder.fail(held);
            }

            boolean learnedThing = switch (stone.type) {
                case BIOME -> tryLearnFromBranch(journal.getLearnedBiomes(), knowledge.biomes, b -> {
                    journal.learnBiome(b);
                    clientMessage(serverPlayer, "biome", b.getPath());
                    return true;
                });

                case STRUCTURE -> tryLearnFromBranch(journal.getLearnedStructures(), knowledge.structures, s -> {
                    journal.learnStructure(s);
                    clientMessage(serverPlayer, "structure", s.getPath());
                    return true;
                });

                case PLAYER -> tryLearnFromBranch(journal.getLearnedPlayers(), knowledge.players, p -> {
                    journal.learnPlayer(p);
                    Optional<String> optName = PlayerHelper.getPlayerName(server, p);
                    if (optName.isPresent()) {
                        clientMessage(serverPlayer, "player", optName.get());
                        return true;
                    } else {
                        return false;
                    }
                });

                case DIMENSION -> tryLearnFromBranch(journal.getLearnedDimensions(), knowledge.dimensions, d -> {
                    journal.learnDimension(d);
                    clientMessage(serverPlayer, "dimension", d.getPath());
                    return true;
                });
            };

            if (learnedThing) {
                // TODO: rune glyph effect and sound effect
            } else {
                player.giveExperienceLevels(10);
            }

            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
        }

        return InteractionResultHolder.consume(held);
    }

    private <T> boolean tryLearnFromBranch(List<T> playerKnowledge, KnowledgeBranch<?, T> branch, Function<T, Boolean> onLearn) {
        if (playerKnowledge.size() < branch.size()) {
            List<T> list = new ArrayList<>(branch.values());
            Collections.shuffle(list, new Random());
            for (T item : list) {
                if (!playerKnowledge.contains(item)) {
                    return onLearn.apply(item);
                }
            }
        }
        return false;
    }

    private void clientMessage(ServerPlayer player, String type, String name) {
        Component component = new TranslatableComponent("gui.strange.knowledge_stones.learned_" + type, StringHelper.snakeToPretty(name, true));
        player.displayClientMessage(component, true);
    }

    public enum Type implements ICharmEnum {
        BIOME,
        STRUCTURE,
        PLAYER,
        DIMENSION;
    }
}
