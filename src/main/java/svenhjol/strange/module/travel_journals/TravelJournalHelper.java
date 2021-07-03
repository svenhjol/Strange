package svenhjol.strange.module.travel_journals;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class TravelJournalHelper {
    public static List<TravelJournalEntry> getEntriesFromNbtList(ListTag nbt) {
        List<TravelJournalEntry> entries = new ArrayList<>();

        for (Tag entryNbt : nbt) {
            TravelJournalEntry entry = new TravelJournalEntry((CompoundTag)entryNbt);
            entries.add(entry);
        }

        return entries;
    }
}
