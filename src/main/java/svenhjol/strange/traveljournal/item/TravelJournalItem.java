package svenhjol.strange.traveljournal.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.ItemNBTHelper;
import svenhjol.strange.traveljournal.client.screen.TravelJournalScreen;

import javax.annotation.Nullable;

public class TravelJournalItem extends MesonItem
{
    public static final String ENTRIES = "entries";
    public static final String POS = "pos";
    public static final String DIM = "dim";
    public static final String NAME = "name";
    public static final String COLOR = "color";
    public static final String PAGE = "page";

    public TravelJournalItem(MesonModule module)
    {
        super(module, "travel_journal", new Item.Properties()
            .group(ItemGroup.MISC)
            .maxStackSize(1)
        );
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    {
        ItemStack held = player.getHeldItem(hand);
        ActionResultType result;

        if (player.isSneaking()) {
            result = ActionResultType.PASS;
        } else {
            if (world.isRemote) {
                Minecraft.getInstance().displayGuiScreen(new TravelJournalScreen(player, hand));
            }

            result = ActionResultType.SUCCESS;
        }

        return new ActionResult<>(result, held);
    }

    public static int getPage(ItemStack stack)
    {
        return ItemNBTHelper.getInt(stack, PAGE, 1);
    }

    public static void setPage(ItemStack stack, int page)
    {
        ItemNBTHelper.setInt(stack, PAGE, page);
    }

    public static CompoundNBT addEntry(ItemStack stack, String id, BlockPos pos, int dim)
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putLong(POS, pos.toLong());
        tag.putInt(DIM, dim);
        tag.putString(NAME, I18n.format("travel_journal.strange.new_entry"));

        CompoundNBT entries = ItemNBTHelper.getCompound(stack, ENTRIES);
        entries.put(id, tag);

        ItemNBTHelper.setCompound(stack, ENTRIES, entries);
        return entries;
    }

    public static CompoundNBT updateEntry(ItemStack stack, String id, BlockPos pos, int dim, String name, int color)
    {
        CompoundNBT entries = getEntries(stack);
        if (entries.get(id) != null) {
            CompoundNBT entry = (CompoundNBT)entries.get(id);
            if (entry != null) {
                entry.putLong(POS, pos.toLong());
                entry.putString(NAME, name);
                entry.putInt(COLOR, color);
                entries.put(id, entry);
                ItemNBTHelper.setCompound(stack, ENTRIES, entries);
            }
        }
        return entries;
    }

    public static CompoundNBT deleteEntry(ItemStack stack, String id)
    {
        CompoundNBT entries = getEntries(stack);
        if (entries.get(id) != null) {
            entries.remove(id);
            ItemNBTHelper.setCompound(stack, ENTRIES, entries);
        }
        return entries;
    }

    @Nullable
    public static CompoundNBT getEntry(ItemStack stack, String id)
    {
        CompoundNBT entries = getEntries(stack);
        if (entries.get(id) != null) {
            return (CompoundNBT)entries.get(id);
        }
        return null;
    }

    @Nullable
    public static BlockPos getPos(CompoundNBT entry)
    {
        if (entry.getLong(POS) != 0) {
            return BlockPos.fromLong(entry.getLong(POS));
        }
        return null;
    }

    public static CompoundNBT getEntries(ItemStack stack)
    {
        CompoundNBT entries = ItemNBTHelper.getCompound(stack, ENTRIES);
        return entries;
    }
}
