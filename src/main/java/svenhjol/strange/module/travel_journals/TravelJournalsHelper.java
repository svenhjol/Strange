package svenhjol.strange.module.travel_journals;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;

public class TravelJournalsHelper {
    public static List<TravelJournalEntry> getEntriesFromNbtList(NbtList tag) {
        List<TravelJournalEntry> entries = new ArrayList<>();

        for (NbtElement entryTag : tag) {
            TravelJournalEntry entry = new TravelJournalEntry((NbtCompound)entryTag);
            entries.add(entry);
        }

        return entries;
    }
}
