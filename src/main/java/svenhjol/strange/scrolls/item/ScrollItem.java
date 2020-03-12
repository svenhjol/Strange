package svenhjol.strange.scrolls.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.PlayerQueueHandler;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.scrolls.message.ClientScrollAction;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.module.Scrolls;
import svenhjol.strange.scrolls.quest.Quest;
import svenhjol.strange.scrolls.quest.iface.IQuest;

public class ScrollItem extends MesonItem {
    private static final String QUEST = "quest";
    private static final String TIER = "tier";
    private static final String VALUE = "value";

    public ScrollItem(MesonModule module) {
        super(module, "scroll", new Item.Properties()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxStackSize(1)
        );

        // allows different item icons to be shown. Each item icon has a float ref (see model)
        addPropertyOverride(new ResourceLocation("tier"), (stack, world, entity) -> {
            int tier = getTier(stack);
            return tier / 10.0F;
        });
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (group == ItemGroup.SEARCH) {
            for (int i = 1; i <= Scrolls.MAX_TIERS; i++) {
                ItemStack scroll = ScrollItem.putTier(new ItemStack(Scrolls.item), i);
                items.add(scroll);
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        IQuest quest;
        ActionResultType result;
        ItemStack scroll = playerIn.getHeldItem(handIn);

        if (PlayerHelper.isCrouching(playerIn)) {
            result = ActionResultType.FAIL;
        } else if (worldIn.getDimension().getType() != DimensionType.OVERWORLD) {
            result = ActionResultType.FAIL;
        } else {
            playerIn.getCooldownTracker().setCooldown(this, 40);
            if (!worldIn.isRemote) {

                // if not populated yet, generate a quest and set the stack name
                if (!hasQuestTag(scroll)) {
                    // there isn't a quest, make one
                    quest = new Quest();
                    quest.setTier(Math.max(getTier(scroll), 1));
                    putQuest(scroll, quest);
                }

                if (!hasPopulatedQuest(scroll)) {
                    float value = Math.max(0.0F, getValue(scroll));

                    if (playerIn.getLuck() > 0)
                        value += worldIn.rand.nextFloat() + 0.25F;

                    final IQuest q = Quests.generate(worldIn, playerIn.getPosition(), value, getQuest(scroll));
                    if (q == null) {
                        Strange.LOG.warn("No quests available, cannot generate!");
                        return new ActionResult<>(ActionResultType.FAIL, scroll);
                    }

                    putQuest(scroll, q);
                    scroll.setDisplayName(new TranslationTextComponent(getQuest(scroll).getTitle()));

                    PlayerQueueHandler.add(worldIn.getGameTime(), playerIn,
                        p -> Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendTo(new ClientScrollAction(q.getId(), handIn), (ServerPlayerEntity) p));

                    return new ActionResult<>(ActionResultType.SUCCESS, scroll);
                }

                quest = getQuest(scroll);

                if (!quest.getId().isEmpty())
                    Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendTo(new ClientScrollAction(quest.getId(), handIn), (ServerPlayerEntity) playerIn); // open the screen

                result = ActionResultType.SUCCESS;
            } else {
                result = ActionResultType.PASS;
            }
        }

        return new ActionResult<>(result, scroll);
    }

    public static IQuest getQuest(ItemStack scroll) {
        IQuest quest = new Quest();
        quest.fromNBT(getQuestTag(scroll));
        return quest;
    }

    public static CompoundNBT getQuestTag(ItemStack scroll) {
        CompoundNBT tag = scroll.getOrCreateTag();
        return (CompoundNBT) tag.get(QUEST);
    }

    public static int getTier(ItemStack scroll) {
        return scroll.getOrCreateTag().getInt(TIER);
    }

    public static float getValue(ItemStack scroll) {
        return scroll.getOrCreateTag().getFloat(VALUE);
    }

    public static boolean hasPopulatedQuest(ItemStack stack) {
        IQuest quest = getQuest(stack);
        return !quest.getCriteria().getConditions().isEmpty();
    }

    public static boolean hasQuestTag(ItemStack scroll) {
        return scroll.getOrCreateTag().contains(QUEST);
    }

    public static void putQuest(ItemStack scroll, IQuest quest) {
        CompoundNBT tag = scroll.getOrCreateTag();
        tag.put(QUEST, quest.toNBT());
    }

    public static ItemStack putTier(ItemStack scroll, int tier) {
        scroll.getOrCreateTag().putInt(TIER, tier);
        scroll.setDisplayName(new TranslationTextComponent("item.strange.scroll_tier" + tier));
        return scroll;
    }

    public static void putValue(ItemStack scroll, float value) {
        CompoundNBT tag = scroll.getOrCreateTag();
        tag.putFloat(VALUE, value);
    }
}
