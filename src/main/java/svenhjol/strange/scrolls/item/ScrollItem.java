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
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.handler.PlayerQueueHandler;
import svenhjol.strange.scrolls.message.ClientScrollAction;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.module.Scrolls;
import svenhjol.strange.scrolls.quest.Quest;
import svenhjol.strange.scrolls.quest.iface.IQuest;

public class ScrollItem extends MesonItem
{
    private static final String QUEST = "quest";
    private static final String TIER = "tier";

    public ScrollItem(MesonModule module)
    {
        super(module, "scroll", new Item.Properties()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxStackSize(1)
        );

        // allows different item icons to be shown. Each item icon has a float ref (see model)
        addPropertyOverride(new ResourceLocation("element"), (stack, world, entity) -> {
            int tier = getTier(stack);
            return tier / 10.0F;
        });
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
    {
        if (group == ItemGroup.SEARCH) {
            for (int i = 1; i <= Scrolls.MAX_TIERS; i++) { ;
                ItemStack scroll = ScrollItem.putTier(new ItemStack(Scrolls.item), i);
                items.add(scroll);
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn)
    {
        ItemStack stack = playerIn.getHeldItem(handIn);
        ActionResultType result;

        if (playerIn.isSneaking()) {
            result = ActionResultType.FAIL;
        } else {
            playerIn.getCooldownTracker().setCooldown(this, 40);

            IQuest quest;
            if (!worldIn.isRemote) {

                // if not populated yet, generate a quest and set the stack name
                if (!hasTag(stack)) {
                    // there isn't a quest, make one
                    int tier = Math.max(getTier(stack), 1);
                    quest = new Quest();
                    quest.setTier(tier);
                    putQuest(stack, quest);
                }

                if (!hasPopulatedQuest(stack)) {
                    final IQuest q = Quests.generate(worldIn, playerIn.getPosition(), getQuest(stack));
                    putQuest(stack, q);
                    stack.setDisplayName(new TranslationTextComponent(getQuest(stack).getTitle()));

                    PlayerQueueHandler.add(worldIn.getGameTime(), playerIn, p -> {
                        PacketHandler.sendTo(new ClientScrollAction(q.getId(), handIn), (ServerPlayerEntity)p);
                    });

                    return new ActionResult<>(ActionResultType.SUCCESS, stack);
                }

                quest = getQuest(stack);

                if (!quest.getId().isEmpty()) {
                    PacketHandler.sendTo(new ClientScrollAction(quest.getId(), handIn), (ServerPlayerEntity)playerIn); // open the screen
                }
                result = ActionResultType.SUCCESS;
            } else {
                result = ActionResultType.PASS;
            }
        }

        return new ActionResult<>(result, stack);
    }

    public static IQuest getQuest(ItemStack scroll)
    {
        IQuest quest = new Quest();
        quest.fromNBT( getTag(scroll) );
        return quest;
    }

    public static void putQuest(ItemStack scroll, IQuest quest)
    {
        putTag(scroll, quest.toNBT());
    }

    public static boolean hasPopulatedQuest(ItemStack stack)
    {
        IQuest quest = getQuest(stack);
        return !quest.getCriteria().getConditions().isEmpty();
    }

    public static int getTier(ItemStack scroll)
    {
        return scroll.getOrCreateTag().getInt(TIER);
    }

    public static ItemStack putTier(ItemStack scroll, int tier)
    {
        scroll.getOrCreateTag().putInt(TIER, tier);
        scroll.setDisplayName(new TranslationTextComponent("item.strange.scroll_tier" + tier));
        return scroll;
    }

    public static boolean hasTag(ItemStack scroll)
    {
        return scroll.hasTag() && scroll.getTag().contains(QUEST);
    }

    public static CompoundNBT getTag(ItemStack scroll)
    {
        CompoundNBT tag = scroll.getOrCreateTag();
        return (CompoundNBT)tag.get(QUEST);
    }

    public static void putTag(ItemStack scroll, CompoundNBT data)
    {
        CompoundNBT tag = scroll.getOrCreateTag();
        tag.put(QUEST, data);
    }
}
