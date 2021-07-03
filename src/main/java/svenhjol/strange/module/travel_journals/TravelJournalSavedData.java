package svenhjol.strange.module.travel_journals;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import svenhjol.charm.Charm;
import svenhjol.charm.helper.DimensionHelper;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings({"unused", "deprecation"})
public class TravelJournalSavedData extends SavedData {
    private Map<UUID, List<TravelJournalEntry>> playerJournalEntries = new HashMap<>();

    public TravelJournalSavedData(ServerLevel world) {
        setDirty();
    }

    public Optional<TravelJournalEntry> getJournalEntry(Player player, @Nullable TravelJournalEntry entry) {
        if (entry == null)
            return Optional.empty();

        UUID uuid = player.getUUID();
        List<TravelJournalEntry> entries = playerJournalEntries.getOrDefault(uuid, new ArrayList<>());
        return entries.stream().filter(e -> e.id.equals(entry.id)).findFirst();
    }

    @Nullable
    public TravelJournalEntry updateJournalEntry(Player player, TravelJournalEntry entry) {
        Optional<TravelJournalEntry> optionalEntry = getJournalEntry(player, entry);
        if (optionalEntry.isEmpty())
            return null;

        TravelJournalEntry updated = optionalEntry.get();
        updated.name = entry.name;
        updated.pos = entry.pos;
        updated.dim = entry.dim;
        updated.color = entry.color;

        setDirty();
        return updated;
    }

    public void deleteJournalEntry(Player player, TravelJournalEntry entry) {
        Optional<TravelJournalEntry> optionalEntry = getJournalEntry(player, entry);
        if (optionalEntry.isEmpty())
            return;

        TravelJournalEntry deleted = optionalEntry.get();
        List<TravelJournalEntry> entries = playerJournalEntries.get(player.getUUID());
        entries.remove(deleted);

        setDirty();
    }

    @Nullable
    public TravelJournalEntry addJournalEntry(Player player, TravelJournalEntry entry) {
        UUID uuid = player.getUUID();
        if (!playerJournalEntries.containsKey(uuid))
            playerJournalEntries.put(uuid, new ArrayList<>());

        List<TravelJournalEntry> entries = playerJournalEntries.get(uuid);

        if (entries.size() >= TravelJournals.MAX_ENTRIES) {
            Charm.LOG.warn("Too many journal entries for player " + uuid.toString());
            return null;
        }

        entries.add(entry);
        setDirty();
        return entry;
    }

    @Nullable
    public TravelJournalEntry initJournalEntry(Player player) {
        String name = new TranslatableComponent("gui.strange.travel_journal.new_entry").getString();

        BlockPos pos = player.blockPosition();
        ResourceLocation dim = DimensionHelper.getDimension(player.level);
        TravelJournalEntry entry = new TravelJournalEntry(name, pos, dim, 15);

        return addJournalEntry(player, entry);
    }

    public static TravelJournalSavedData fromNbt(ServerLevel world, CompoundTag nbt) {
        TravelJournalSavedData savedData = new TravelJournalSavedData(world);

        // inflate into hashmap
        savedData.playerJournalEntries = new HashMap<>();

        Set<String> uuids = nbt.getAllKeys();
        for (String uuid : uuids) {
            UUID player = UUID.fromString(uuid);
            ListTag listTag = nbt.getList(uuid, 10);
            List<TravelJournalEntry> entries = savedData.unserializePlayerEntries(listTag);
            savedData.playerJournalEntries.put(player, entries);
        }

        return savedData;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        // serialize all player journal entries into the master tag
        for (UUID uuid : playerJournalEntries.keySet()) {
            ListTag listTag = serializePlayerEntries(uuid);
            nbt.put(uuid.toString(), listTag);
        }

        return nbt;
    }

    public ListTag serializePlayerEntries(UUID uuid) {
        List<TravelJournalEntry> entries = playerJournalEntries.getOrDefault(uuid, new ArrayList<>());
        ListTag tag = new ListTag();

        for (TravelJournalEntry entry : entries) {
            tag.add(entry.toNbt());
        }

        return tag;
    }

    public List<TravelJournalEntry> unserializePlayerEntries(ListTag tag) {
        List<TravelJournalEntry> entries = new ArrayList<>();
        for (Tag t : tag) {
            entries.add(new TravelJournalEntry((CompoundTag)t));
        }

        return entries;
    }

    public static String nameFor(DimensionType dimensionType) {
        return "strange_traveljournals" + dimensionType.getFileSuffix();
    }
}
