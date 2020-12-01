package svenhjol.strange.traveljournals;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class TravelJournalsHelper {
    public static List<JournalEntry> getEntriesFromListTag(ListTag tag) {
        List<JournalEntry> entries = new ArrayList<>();

        for (Tag entryTag : tag) {
            JournalEntry entry = new JournalEntry((CompoundTag)entryTag);
            entries.add(entry);
        }

        return entries;
    }
}
