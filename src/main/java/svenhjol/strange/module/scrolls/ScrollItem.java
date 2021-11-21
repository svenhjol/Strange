package svenhjol.strange.module.scrolls;

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
import svenhjol.strange.module.quests.QuestDefinition;
import svenhjol.strange.module.quests.Quests;

import java.util.Optional;
import java.util.Random;

public class ScrollItem extends CharmItem {
    private static final String TAG_QUEST = "quest";
    private static final String TAG_DIFFICULTY = "difficulty";

    private final int tier;

    public ScrollItem(CharmModule module, int tier) {
        super(module, Quests.TIER_NAMES.get(tier) + "_scroll", new Item.Properties()
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
            QuestData quests = Quests.getQuestData().orElseThrow();
            Optional<Quest> quest = quests.get(questId);

            if (quest.isEmpty()) {
                LogHelper.warn(this.getClass(), "No matching quest with id " + questId + ", giving up");
                return destroy(serverPlayer, scroll);
            }

            quest.get().start(player);
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
        scroll.getOrCreateTag().putString(TAG_QUEST, quest);
    }

    public static void setScrollDifficulty(ItemStack scroll, float difficulty) {
        scroll.getOrCreateTag().putFloat(TAG_DIFFICULTY, difficulty);
    }

    public static String getScrollQuest(ItemStack scroll) {
        return scroll.getOrCreateTag().getString(TAG_QUEST);
    }

    public static float getScrollDifficulty(ItemStack scroll) {
        float difficulty = scroll.getOrCreateTag().getFloat(TAG_DIFFICULTY);
        return difficulty == 0 ? 1.0F : difficulty;
    }
}
