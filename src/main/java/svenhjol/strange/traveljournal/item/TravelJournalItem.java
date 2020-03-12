package svenhjol.strange.traveljournal.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.ItemNBTHelper;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.strange.traveljournal.Entry;
import svenhjol.strange.traveljournal.module.TravelJournal;

import javax.annotation.Nullable;

public class TravelJournalItem extends MesonItem {
    public static final int MAX_NAME_LENGTH = 24;
    public static final int SCREENSHOT_DISTANCE = 10;

    public static final String ENTRIES = "entries";
    public static final String PAGE = "page";

    public TravelJournalItem(MesonModule module) {
        super(module, "travel_journal", new Item.Properties()
            .group(ItemGroup.MISC)
            .maxStackSize(1)
        );
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack held = player.getHeldItem(hand);
        ActionResultType result;

        if (PlayerHelper.isCrouching(player)) {
            result = ActionResultType.PASS;
        } else {
            if (world.isRemote) {
                TravelJournal.client.openTravelJournal(hand);
            }
            result = ActionResultType.SUCCESS;
        }

        return new ActionResult<>(result, held);
    }

    public static int getPage(ItemStack stack) {
        return ItemNBTHelper.getInt(stack, PAGE, 1);
    }

    public static void setPage(ItemStack stack, int page) {
        ItemNBTHelper.setInt(stack, PAGE, page);
    }

    public static void addEntry(ItemStack stack, Entry entry) {
        CompoundNBT tag = entry.toNBT();
        CompoundNBT entries = ItemNBTHelper.getCompound(stack, ENTRIES);

        if (entries.keySet().size() < TravelJournal.maxEntries) {
            entries.put(entry.id, tag);
            ItemNBTHelper.setCompound(stack, ENTRIES, entries);
        }
    }

    public static void updateEntry(ItemStack stack, Entry entry) {
        CompoundNBT entries = getEntries(stack);
        if (entries.get(entry.id) != null) {
            CompoundNBT nbt = (CompoundNBT) entries.get(entry.id);
            if (nbt != null) {
                CompoundNBT updated = entry.toNBT();
                entries.put(entry.id, updated);
                ItemNBTHelper.setCompound(stack, ENTRIES, entries);
            }
        }
    }

    public static void deleteEntry(ItemStack stack, Entry entry) {
        CompoundNBT entries = getEntries(stack);
        if (entries.get(entry.id) != null) {
            entries.remove(entry.id);
            ItemNBTHelper.setCompound(stack, ENTRIES, entries);
        }
    }

    @Nullable
    public static CompoundNBT getEntry(ItemStack stack, String id) {
        CompoundNBT entries = getEntries(stack);
        if (entries.get(id) != null) {
            return (CompoundNBT) entries.get(id);
        }
        return null;
    }

    public static CompoundNBT getEntries(ItemStack stack) {
        return ItemNBTHelper.getCompound(stack, ENTRIES);
    }
}
