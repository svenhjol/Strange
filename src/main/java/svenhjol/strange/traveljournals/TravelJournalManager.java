package svenhjol.strange.traveljournals;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import svenhjol.charm.Charm;
import svenhjol.charm.base.helper.DimensionHelper;

import javax.annotation.Nullable;
import java.util.*;

public class TravelJournalManager extends PersistentState {
    private Map<UUID, List<JournalEntry>> playerJournalEntries = new HashMap<>();
    private final World world;

    public TravelJournalManager(ServerWorld world) {
        this.world = world;
        markDirty();
    }

    public Optional<JournalEntry> getJournalEntry(PlayerEntity player, @Nullable JournalEntry entry) {
        if (entry == null)
            return Optional.empty();

        UUID uuid = player.getUuid();
        List<JournalEntry> entries = playerJournalEntries.getOrDefault(uuid, new ArrayList<>());
        return entries.stream().filter(e -> e.id.equals(entry.id)).findFirst();
    }

    @Nullable
    public JournalEntry updateJournalEntry(PlayerEntity player, JournalEntry entry) {
        Optional<JournalEntry> optionalEntry = getJournalEntry(player, entry);
        if (!optionalEntry.isPresent())
            return null;

        JournalEntry updated = optionalEntry.get();
        updated.name = entry.name;
        updated.pos = entry.pos;
        updated.dim = entry.dim;
        updated.color = entry.color;

        markDirty();
        return updated;
    }

    public void deleteJournalEntry(PlayerEntity player, JournalEntry entry) {
        Optional<JournalEntry> optionalEntry = getJournalEntry(player, entry);
        if (!optionalEntry.isPresent())
            return;

        JournalEntry deleted = optionalEntry.get();
        List<JournalEntry> entries = playerJournalEntries.get(player.getUuid());
        entries.remove(deleted);

        markDirty();
    }

    @Nullable
    public JournalEntry addJournalEntry(PlayerEntity player, JournalEntry entry) {
        UUID uuid = player.getUuid();
        if (!playerJournalEntries.containsKey(uuid))
            playerJournalEntries.put(uuid, new ArrayList<>());

        List<JournalEntry> entries = playerJournalEntries.get(uuid);

        if (entries.size() >= TravelJournals.MAX_ENTRIES) {
            Charm.LOG.warn("Too many journal entries for player " + uuid.toString());
            return null;
        }

        entries.add(entry);
        markDirty();
        return entry;
    }

    @Nullable
    public JournalEntry initJournalEntry(PlayerEntity player) {
        String name = new TranslatableText("gui.strange.travel_journal.new_entry").getString();

        BlockPos pos = player.getBlockPos();
        Identifier dim = DimensionHelper.getDimension(player.world);
        JournalEntry entry = new JournalEntry(name, pos, dim, 15);

        return addJournalEntry(player, entry);
    }

    public static TravelJournalManager fromTag(ServerWorld world, NbtCompound tag) {
        TravelJournalManager manager = new TravelJournalManager(world);

        // inflate into hashmap
        manager.playerJournalEntries = new HashMap<>();

        Set<String> uuids = tag.getKeys();
        for (String uuid : uuids) {
            UUID player = UUID.fromString(uuid);
            NbtList listTag = tag.getList(uuid, 10);
            List<JournalEntry> entries = manager.unserializePlayerEntries(listTag);
            manager.playerJournalEntries.put(player, entries);
        }

        return manager;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        // serialize all player journal entries into the master tag
        for (UUID uuid : playerJournalEntries.keySet()) {
            NbtList listTag = serializePlayerEntries(uuid);
            tag.put(uuid.toString(), listTag);
        }

        return tag;
    }

    public NbtList serializePlayerEntries(UUID uuid) {
        List<JournalEntry> entries = playerJournalEntries.getOrDefault(uuid, new ArrayList<>());
        NbtList tag = new NbtList();

        for (JournalEntry entry : entries) {
            tag.add(entry.toTag());
        }

        return tag;
    }

    public List<JournalEntry> unserializePlayerEntries(NbtList tag) {
        List<JournalEntry> entries = new ArrayList<>();
        for (NbtElement t : tag) {
            entries.add(new JournalEntry((NbtCompound)t));
        }

        return entries;
    }

    public static String nameFor(DimensionType dimensionType) {
        return "traveljournal" + dimensionType.getSuffix();
    }
}
