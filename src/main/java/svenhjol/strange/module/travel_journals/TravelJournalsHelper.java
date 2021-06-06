package svenhjol.strange.module.travel_journals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class TravelJournalsHelper {
    public static List<TravelJournalEntry> getEntriesFromNbtList(ListTag tag) {
        List<TravelJournalEntry> entries = new ArrayList<>();

        for (Tag entryTag : tag) {
            TravelJournalEntry entry = new TravelJournalEntry((CompoundTag)entryTag);
            entries.add(entry);
        }

        return entries;
    }
}
