package svenhjol.strange.module.travel_journals;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import svenhjol.charm.Charm;
import svenhjol.charm.helper.DimensionHelper;

import javax.annotation.Nullable;
import java.util.*;

public class TravelJournalManager extends SavedData {
    private Map<UUID, List<TravelJournalEntry>> playerJournalEntries = new HashMap<>();
    private final Level world;

    public TravelJournalManager(ServerLevel world) {
        this.world = world;
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
        if (!optionalEntry.isPresent())
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
        if (!optionalEntry.isPresent())
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

    public static TravelJournalManager fromTag(ServerLevel world, CompoundTag tag) {
        TravelJournalManager manager = new TravelJournalManager(world);

        // inflate into hashmap
        manager.playerJournalEntries = new HashMap<>();

        Set<String> uuids = tag.getAllKeys();
        for (String uuid : uuids) {
            UUID player = UUID.fromString(uuid);
            ListTag listTag = tag.getList(uuid, 10);
            List<TravelJournalEntry> entries = manager.unserializePlayerEntries(listTag);
            manager.playerJournalEntries.put(player, entries);
        }

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        // serialize all player journal entries into the master tag
        for (UUID uuid : playerJournalEntries.keySet()) {
            ListTag listTag = serializePlayerEntries(uuid);
            tag.put(uuid.toString(), listTag);
        }

        return tag;
    }

    public ListTag serializePlayerEntries(UUID uuid) {
        List<TravelJournalEntry> entries = playerJournalEntries.getOrDefault(uuid, new ArrayList<>());
        ListTag tag = new ListTag();

        for (TravelJournalEntry entry : entries) {
            tag.add(entry.toTag());
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
