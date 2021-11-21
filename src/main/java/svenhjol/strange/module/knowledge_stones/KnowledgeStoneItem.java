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
import svenhjol.charm.helper.StringHelper;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.JournalHelper;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeData;
import svenhjol.strange.module.knowledge.KnowledgeHelper;
import svenhjol.strange.module.knowledge.KnowledgeHelper.LearnableKnowledgeType;

public class KnowledgeStoneItem extends CharmItem {
    private final LearnableKnowledgeType type;

    public KnowledgeStoneItem(CharmModule module, LearnableKnowledgeType type) {
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
        KnowledgeData knowledge = Knowledge.getKnowledgeData().orElseThrow();

        if (!level.isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            MinecraftServer server = serverPlayer.getServer();
            if (server == null) {
                return InteractionResultHolder.fail(held);
            }

            boolean learnedThing = false;

            switch (stone.type) {
                case RUNE -> {
                    learnedThing = JournalHelper.learnNextLearnableRune(journal);
                    if (learnedThing) {
                        sendClientMessage(serverPlayer, "rune", "");
                    }
                }
                case BIOME -> learnedThing = KnowledgeHelper.tryLearnFromBranch(journal.getLearnedBiomes(), knowledge.biomes, b -> {
                    journal.learnBiome(b);
                    sendClientMessage(serverPlayer, "biome", b.getPath());
                    return true;
                });
                case STRUCTURE -> learnedThing = KnowledgeHelper.tryLearnFromBranch(journal.getLearnedStructures(), knowledge.structures, s -> {
                    journal.learnStructure(s);
                    sendClientMessage(serverPlayer, "structure", s.getPath());
                    return true;
                });
                case DIMENSION -> learnedThing = KnowledgeHelper.tryLearnFromBranch(journal.getLearnedDimensions(), knowledge.dimensions, d -> {
                    journal.learnDimension(d);
                    sendClientMessage(serverPlayer, "dimension", d.getPath());
                    return true;
                });
            }

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

    private void sendClientMessage(ServerPlayer player, String type, String name) {
        Component component = new TranslatableComponent("gui.strange.knowledge_stones.learned_" + type, StringHelper.snakeToPretty(name, true));
        player.displayClientMessage(component, true);
    }
}
