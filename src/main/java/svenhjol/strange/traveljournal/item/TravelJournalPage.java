package svenhjol.strange.traveljournal.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.ItemNBTHelper;
import svenhjol.strange.traveljournal.Entry;

public class TravelJournalPage extends MesonItem {
    public static final String ENTRY = "entry";

    public TravelJournalPage(MesonModule module) {
        super(module, "travel_journal_page", new Item.Properties()
            .group(ItemGroup.MISC)
            .maxStackSize(1)
        );
    }

    public static Entry getEntry(ItemStack stack) {
        return new Entry(ItemNBTHelper.getCompound(stack, ENTRY));
    }

    public static void setEntry(ItemStack stack, Entry entry) {
        ItemNBTHelper.setCompound(stack, ENTRY, entry.toNBT());
    }
}
