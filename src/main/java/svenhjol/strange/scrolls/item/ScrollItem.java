package svenhjol.strange.scrolls.item;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.strange.scrolls.client.screen.ScrollScreen;
import svenhjol.strange.scrolls.module.Quests.QuestType;
import svenhjol.strange.scrolls.quest.IQuest;
import svenhjol.strange.scrolls.quest.Quest;
import svenhjol.strange.scrolls.quest.action.Action;
import svenhjol.strange.scrolls.quest.action.Pickup;

import java.util.UUID;

public class ScrollItem extends MesonItem
{
    private int tier;

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
                Minecraft mc = Minecraft.getInstance();

                writeExtendedQuestData(stack);
                mc.deferTask(() -> mc.displayGuiScreen(new ScrollScreen(playerIn, stack)));
            }

            // TODO flicker when opening another gui
            result = ActionResultType.SUCCESS;
        }

        return new ActionResult<>(result, stack);
    }

    public static boolean isValidScroll(ItemStack stack)
    {
        return stack.getItem() instanceof ScrollItem;
    }

    public static void createQuest(ItemStack stack, int tier, QuestType type, long purchaseTime, UUID seller)
    {
        if (!isValidScroll(stack)) return;

        IQuest quest = new Quest();
        quest.setSeller(seller);
        quest.setTier(tier);
        quest.setType(type);

        stack.setDisplayName(new StringTextComponent("Scroll of " + type.toString()));

        putTag(stack, quest.toNBT());
    }

    public static void writeExtendedQuestData(ItemStack stack)
    {
        IQuest quest = new Quest();
        quest.fromNBT( getTag(stack) );

        quest.setDescription(new StringTextComponent("Description here!"));

        Action<Pickup> getCoal = Action.factory(Pickup.class, quest);
        getCoal.getDelegate()
            .setStack(new ItemStack(Items.COAL))
            .setCount(10);

        Action<Pickup> getEmeralds = Action.factory(Pickup.class, quest);
        getEmeralds.getDelegate()
            .setStack(new ItemStack(Items.EMERALD))
            .setCount(1);

        quest.getCriteria()
            .addAction(getCoal)
            .addAction(getEmeralds);

        putTag(stack, quest.toNBT());
    }

    public static IQuest getQuest(ItemStack stack)
    {
        IQuest quest = new Quest();
        quest.fromNBT( getTag(stack) );
        return quest;
    }

    private static CompoundNBT getTag(ItemStack stack)
    {
        if (!isValidScroll(stack)) return new CompoundNBT();

        CompoundNBT tag = stack.getTag();
        if (tag == null) {
            tag = new CompoundNBT();
        }
        return (CompoundNBT)tag.get("quest");
    }

    private static CompoundNBT putTag(ItemStack stack, CompoundNBT data)
    {
        CompoundNBT tag = stack.getTag();
        if (tag == null) {
            tag = new CompoundNBT();
        }

        tag.put("quest", data);
        return tag;
    }
}
