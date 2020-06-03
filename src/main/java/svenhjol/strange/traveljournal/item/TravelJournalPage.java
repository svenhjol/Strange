package svenhjol.strange.traveljournal.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.ItemNBTHelper;
import svenhjol.strange.traveljournal.Entry;
import svenhjol.strange.traveljournal.module.TravelJournal;

public class TravelJournalPage extends MesonItem {
    public static final String ENTRY = "entry";
    public static final String DISPLAY = "display";

    public TravelJournalPage(MesonModule module) {
        super(module, "travel_journal_page", new Item.Properties()
            .group(ItemGroup.MISC)
            .maxStackSize(1)
        );
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack held = playerIn.getHeldItem(handIn);

        // try and add the page if the player is crouching while holding a travel journal
        if (playerIn.isCrouching()) {
            Hand opposite = handIn == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
            ItemStack heldInOpposite = playerIn.getHeldItem(opposite);

            if (heldInOpposite.getItem() == TravelJournal.item) {
                Entry entry = getEntry(held);

                if (TravelJournalItem.getEntry(heldInOpposite, entry.id) != null) {
                    playerIn.sendStatusMessage(new TranslationTextComponent("gui.strange.travel_journal.page_exists"), true);
                    return new ActionResult<>(ActionResultType.PASS, held);
                }

                boolean didAdd = TravelJournalItem.addEntry(heldInOpposite, entry);
                if (didAdd) {
                    playerIn.playSound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0F, 1.0F);
                    playerIn.sendStatusMessage(new TranslationTextComponent("gui.strange.travel_journal.added_page"), true);

                    if (!worldIn.isRemote)
                        held.shrink(1);

                    return new ActionResult<>(ActionResultType.SUCCESS, ItemStack.EMPTY);
                }
            }
        }

        // flip the page if normal right click
        int display = getDisplay(held);
        setDisplay(held, display == 0 ? 1 : 0); // boolean behavior for now but we might add more functionality in future

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    public static Entry getEntry(ItemStack stack) {
        return new Entry(ItemNBTHelper.getCompound(stack, ENTRY));
    }

    public static void setEntry(ItemStack stack, Entry entry) {
        ItemNBTHelper.setCompound(stack, ENTRY, entry.toNBT());
    }

    public static int getDisplay(ItemStack stack) {
        return ItemNBTHelper.getInt(stack, DISPLAY, 0);
    }

    public static void setDisplay(ItemStack stack, int display) {
        ItemNBTHelper.setInt(stack, DISPLAY, display);
    }
}
