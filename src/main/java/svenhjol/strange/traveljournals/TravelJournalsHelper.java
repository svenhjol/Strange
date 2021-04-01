package svenhjol.strange.traveljournals;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;

public class TravelJournalsHelper {
    public static List<JournalEntry> getEntriesFromNbtList(NbtList tag) {
        List<JournalEntry> entries = new ArrayList<>();

        for (NbtElement entryTag : tag) {
            JournalEntry entry = new JournalEntry((NbtCompound)entryTag);
            entries.add(entry);
        }

        return entries;
    }
}
