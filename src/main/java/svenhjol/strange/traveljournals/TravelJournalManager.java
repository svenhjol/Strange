package svenhjol.strange.traveljournals;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.charm.Charm;
import svenhjol.charm.base.helper.BiomeHelper;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.strange.Strange;

import javax.annotation.Nullable;
import java.util.*;

public class TravelJournalManager extends PersistentState {
    private Map<UUID, List<JournalEntry>> playerJournalEntries = new HashMap<>();

    public TravelJournalManager(ServerWorld world) {
        super(nameFor(world.getDimension()));
        markDirty();
    }

    public Optional<JournalEntry> getJournalEntry(PlayerEntity player, @Nullable String id) {
        if (id == null)
            return Optional.empty();

        UUID uuid = player.getUuid();
        List<JournalEntry> entries = playerJournalEntries.getOrDefault(uuid, new ArrayList<>());
        return entries.stream().filter(e -> e.id.equals(id)).findFirst();
    }

    @Nullable
    public JournalEntry updateJournalEntry(PlayerEntity player, JournalEntry entry) {
        Optional<JournalEntry> optionalEntry = getJournalEntry(player, entry.id);
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
        Optional<JournalEntry> optionalEntry = getJournalEntry(player, entry.id);
        if (!optionalEntry.isPresent())
            return;

        JournalEntry deleted = optionalEntry.get();
        List<JournalEntry> entries = playerJournalEntries.get(player.getUuid());
        entries.remove(deleted);

        markDirty();
    }

    @Nullable
    public JournalEntry addJournalEntry(PlayerEntity player) {
        UUID uuid = player.getUuid();
        if (!playerJournalEntries.containsKey(uuid))
            playerJournalEntries.put(uuid, new ArrayList<>());

        List<JournalEntry> entries = playerJournalEntries.get(uuid);

        if (entries.size() > TravelJournals.MAX_ENTRIES) {
            Charm.LOG.warn("Too many journal entries for player " + uuid.toString());
            return null;
        }

        String id = Strange.MOD_ID + "_" + RandomStringUtils.randomAlphabetic(4);
        String name = "";

        if (!player.world.isClient) {
            Optional<RegistryKey<Biome>> biomeKey = BiomeHelper.getBiomeKeyAtPosition((ServerWorld) player.world, player.getBlockPos());
            if (biomeKey.isPresent()) {
                Identifier biomeId = biomeKey.get().getValue();
                if (biomeId != null)
                    name = new TranslatableText("biome.minecraft." + biomeId.getPath()).getString();
            }
        }

        if (name.isEmpty())
            name = "New location";

        BlockPos pos = player.getBlockPos();
        Identifier dim = DimensionHelper.getDimension(player.world);
        JournalEntry entry = new JournalEntry(id, name, pos, dim, 15);

        entries.add(entry);

        markDirty();
        return entry;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        // inflate into hashmap
        playerJournalEntries = new HashMap<>();

        Set<String> uuids = tag.getKeys();
        for (String uuid : uuids) {
            UUID player = UUID.fromString(uuid);
            ListTag listTag = tag.getList(uuid, 10);
            List<JournalEntry> entries = unserializePlayerEntries(listTag);
            playerJournalEntries.put(player, entries);
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        // serialize all player journal entries into the master tag
        for (UUID uuid : playerJournalEntries.keySet()) {
            ListTag listTag = serializePlayerEntries(uuid);
            tag.put(uuid.toString(), listTag);
        }

        return tag;
    }

    public ListTag serializePlayerEntries(UUID uuid) {
        List<JournalEntry> entries = playerJournalEntries.getOrDefault(uuid, new ArrayList<>());
        ListTag tag = new ListTag();

        for (JournalEntry entry : entries) {
            tag.add(entry.toTag());
        }

        return tag;
    }

    public List<JournalEntry> unserializePlayerEntries(ListTag tag) {
        List<JournalEntry> entries = new ArrayList<>();
        for (Tag t : tag) {
            entries.add(new JournalEntry((CompoundTag)t));
        }

        return entries;
    }

    public static String nameFor(DimensionType dimensionType) {
        return "traveljournal" + dimensionType.getSuffix();
    }
}
