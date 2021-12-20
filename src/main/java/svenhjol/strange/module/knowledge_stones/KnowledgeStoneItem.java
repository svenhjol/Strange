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
import svenhjol.strange.module.journals2.Journal2Data;
import svenhjol.strange.module.journals2.Journals2;
import svenhjol.strange.module.journals2.helper.Journal2Helper;
import svenhjol.strange.module.knowledge2.Knowledge2;
import svenhjol.strange.module.knowledge2.Knowledge2Data;
import svenhjol.strange.module.knowledge2.Learnable;
import svenhjol.strange.module.runes.Tier;

public class KnowledgeStoneItem extends CharmItem {
    private final Learnable type;

    public KnowledgeStoneItem(CharmModule module, Learnable type) {
        super(module, type.getSerializedName() + "_knowledge_stone", new FabricItemSettings()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(64));

        this.type = type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        KnowledgeStoneItem stone = (KnowledgeStoneItem)held.getItem();

        Journal2Data journal = Journals2.getJournal(player).orElseThrow();
        Knowledge2Data knowledge = Knowledge2.getKnowledge().orElseThrow();

        if (!level.isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            MinecraftServer server = serverPlayer.getServer();
            if (server == null) {
                return InteractionResultHolder.fail(held);
            }

            boolean learned = false;

            switch (stone.type) {
                case RUNE -> {
                    int runeval = Journal2Helper.nextLearnableRune(Tier.MASTER, journal);
                    if (runeval > 0) {
                        sendClientMessage(serverPlayer, "rune", String.valueOf((char)(runeval + 96)));
                    }
                    learned = true;
                }
                case BIOME -> learned = Journal2Helper.learn(knowledge.biomeBranch, journal.getLearnedBiomes(), b -> {
                    journal.learnBiome(b);
                    sendClientMessage(serverPlayer, "biome", b.getPath());
                    return true;
                });
                case STRUCTURE -> learned = Journal2Helper.learn(knowledge.structureBranch, journal.getLearnedStructures(), s -> {
                    journal.learnStructure(s);
                    sendClientMessage(serverPlayer, "structure", s.getPath());
                    return true;
                });
                case DIMENSION -> learned = Journal2Helper.learn(knowledge.dimensionBranch, journal.getLearnedDimensions(), d -> {
                    journal.learnDimension(d);
                    sendClientMessage(serverPlayer, "dimension", d.getPath());
                    return true;
                });
            }

            if (learned) {
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
