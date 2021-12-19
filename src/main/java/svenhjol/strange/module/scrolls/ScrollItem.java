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
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.QuestData;
import svenhjol.strange.module.quests.Quests;
import svenhjol.strange.module.quests.definition.QuestDefinition;
import svenhjol.strange.module.quests.helper.QuestHelper;
import svenhjol.strange.module.runes.Tier;

import java.util.Random;

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
        ItemStack scroll = player.getItemInHand(hand);
        if (level.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.PASS, scroll);
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;
        QuestData quests = Quests.getQuestData().orElseThrow();
        if (quests.all(serverPlayer).size() >= QuestHelper.MAX_QUESTS) {
            player.displayClientMessage(new TranslatableComponent("gui.strange.quests.max_reached"), true);
            return new InteractionResultHolder<>(InteractionResult.FAIL, scroll);
        }

        String questId = getScrollQuest(scroll);
        float difficulty = getScrollDifficulty(scroll);

        if (questId.isEmpty()) {
            Random random = new Random();
            QuestDefinition definition = Quests.getRandomDefinition(serverPlayer, tier, random);

            if (definition == null) {
                LogHelper.warn(this.getClass(), "Could not find any definitions, giving up");
                return destroy(serverPlayer, scroll);
            }

            Quest quest = new Quest(definition, difficulty);
            quest.start(player);

        } else {
            var quest = quests.get(questId);
            if (quest == null) {
                LogHelper.warn(this.getClass(), "No matching quest with id " + questId + ", giving up");
                return destroy(serverPlayer, scroll);
            }

            quest.start(player);
        }

        scroll.shrink(1);
        NetworkHelper.sendEmptyPacketToClient(serverPlayer, Scrolls.MSG_CLIENT_OPEN_SCROLL);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, scroll);
    }

    private InteractionResultHolder<ItemStack> destroy(ServerPlayer player, ItemStack scroll) {
        scroll.shrink(1);
        NetworkHelper.sendEmptyPacketToClient(player, Scrolls.MSG_CLIENT_DESTROY_SCROLL);
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
