package svenhjol.strange.module.scrolls;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.Quest.State;
import svenhjol.strange.module.quests.Quests;
import svenhjol.strange.module.quests.helper.QuestHelper;
import svenhjol.strange.module.runes.Tier;

import java.util.Random;

@SuppressWarnings("unused")
public class ScrollItem extends CharmItem {
    private static final String QUEST_TAG = "quest";
    private static final String DIFFICULTY_TAG = "difficulty";

    private final Tier tier;

    public ScrollItem(CharmModule module, Tier tier) {
        super(module, tier.getSerializedName() + "_scroll", new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .rarity(Rarity.COMMON)
            .stacksTo(1));

        this.tier = tier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        Quest quest = null;
        boolean invalidQuest = false;
        var scroll = player.getItemInHand(hand);

        if (level.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.PASS, scroll);
        }

        var serverPlayer = (ServerPlayer) player;
        var quests = Quests.getQuestData().orElseThrow();
        if (quests.all(serverPlayer).size() >= QuestHelper.MAX_QUESTS) {
            player.displayClientMessage(new TranslatableComponent("gui.strange.quests.max_reached"), true);
            return new InteractionResultHolder<>(InteractionResult.FAIL, scroll);
        }

        var questId = getScrollQuest(scroll);
        var difficulty = getScrollDifficulty(scroll);

        if (questId.isEmpty()) {

            // This is a new scroll without an associated paused quest. Generate a new quest.
            var random = new Random();
            var definition = Quests.getRandomDefinition(serverPlayer, tier, random);
            if (definition != null) {

                // Definition is valid, create a new quest.
                quest = new Quest(definition, difficulty);
                quest.start(player);

            } else {

                // No valid definition found, destroy the scroll.
                LogHelper.warn(this.getClass(), "Could not find any definitions, giving up");
                invalidQuest = true;

            }

        } else {

            // The scroll contains the ID of a paused quest, resume it here.
            quest = quests.get(questId);
            if (quest != null) {

                // Quest is valid, resume the quest.
                quest.start(player);

            } else {

                // No valid quest found, destroy the scroll.
                LogHelper.warn(this.getClass(), "No matching quest with id " + questId + ", giving up");
                invalidQuest = true;

            }
        }

        if (invalidQuest || quest.getState() == State.FINISHED) {
            return destroy(serverPlayer, scroll);
        } else if (quest.getState() == State.PAUSED) {
            return new InteractionResultHolder<>(InteractionResult.PASS, scroll);
        } else {
            scroll.shrink(1);
            Scrolls.SERVER_SEND_OPEN_SCROLL.send(serverPlayer);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, scroll);
        }
    }

    private InteractionResultHolder<ItemStack> destroy(ServerPlayer player, ItemStack scroll) {
        scroll.shrink(1);
        Scrolls.SERVER_SEND_DESTROY_SCROLL.send(player);
        return new InteractionResultHolder<>(InteractionResult.FAIL, scroll);
    }

    public static void setScrollQuest(ItemStack scroll, String quest) {
        scroll.getOrCreateTag().putString(QUEST_TAG, quest);
    }

    public static void setScrollDifficulty(ItemStack scroll, float difficulty) {
        scroll.getOrCreateTag().putFloat(DIFFICULTY_TAG, difficulty);
    }

    public static String getScrollQuest(ItemStack scroll) {
        return scroll.getOrCreateTag().getString(QUEST_TAG);
    }

    public static float getScrollDifficulty(ItemStack scroll) {
        float difficulty = scroll.getOrCreateTag().getFloat(DIFFICULTY_TAG);
        return difficulty == 0 ? 1.0F : difficulty;
    }
}
