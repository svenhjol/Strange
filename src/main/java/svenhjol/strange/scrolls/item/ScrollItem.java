package svenhjol.strange.scrolls.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.Quest;
import svenhjol.strange.scrolls.quest.iface.IQuest;

public class ScrollItem extends MesonItem
{
    private int tier;
    private static final String QUEST = "quest";

    public ScrollItem(MesonModule module, int tier)
    {
        super(module, "scroll_tier_" + tier, new Item.Properties()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxStackSize(1)
        );
        this.tier = tier;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn)
    {
        ItemStack stack = playerIn.getHeldItem(handIn);
        ActionResultType result;

        if (playerIn.isSneaking()) {
            result = ActionResultType.FAIL;
        } else {

            if (!worldIn.isRemote) {

                // if not populated yet, generate a quest and set the stack name
                if (!hasTag(stack)) {

                    // there isn't a quest, make one
                    int tier = getTierFromScroll(stack);
                    if (tier < 1) tier = 1;

                    IQuest quest = new Quest();
                    quest.setTier(tier);
                    putQuest(stack, quest);
                }

                if (!hasPopulatedQuest(stack)) {
                    IQuest quest = Quests.generate(worldIn, getQuest(stack));
                    quest.generateId();
                    putQuest(stack, quest);
                    stack.setDisplayName(new StringTextComponent(getQuest(stack).getTitle()));
                }

                Quests.proxy.showQuestScreen(playerIn, stack);
            }

            result = ActionResultType.SUCCESS;
        }

        return new ActionResult<>(result, stack);
    }

    public static boolean isValidScroll(ItemStack stack)
    {
        return stack.getItem() instanceof ScrollItem;
    }

    public static IQuest getQuest(ItemStack stack)
    {
        IQuest quest = new Quest();
        quest.fromNBT( getTag(stack) );
        return quest;
    }

    public static void putQuest(ItemStack stack, IQuest quest)
    {
        putTag(stack, quest.toNBT());
    }

    public static boolean hasPopulatedQuest(ItemStack stack)
    {
        IQuest quest = getQuest(stack);
        return !quest.getCriteria().getConditions().isEmpty();
    }

    public static int getTierFromScroll(ItemStack stack)
    {
        if (hasTag(stack)) {
            IQuest quest = getQuest(stack);
            if (quest.getTier() > 0) return quest.getTier();
        }

        String path = stack.getItem().getRegistryName().getPath();
        if (!path.isEmpty()) {
            String[] split = path.split("_");
            return Integer.parseInt(split[2]);
        }

        return 0;
    }

    public static boolean hasTag(ItemStack stack)
    {
        return stack.hasTag() && stack.getTag().contains(QUEST);
    }

    public static CompoundNBT getTag(ItemStack stack)
    {
        if (!isValidScroll(stack)) return new CompoundNBT();

        CompoundNBT tag = stack.getTag();
        if (tag == null) {
            tag = new CompoundNBT();
        }
        return (CompoundNBT)tag.get(QUEST);
    }

    public static CompoundNBT putTag(ItemStack stack, CompoundNBT data)
    {
        CompoundNBT tag = stack.getTag();
        if (tag == null) {
            tag = new CompoundNBT();
        }

        tag.put(QUEST, data);
        stack.setTag(tag);
        return tag;
    }
}
