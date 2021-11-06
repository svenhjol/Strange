package svenhjol.strange.module.journals;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.strange.module.journals.data.JournalLocation;
import svenhjol.strange.module.journals.data.JournalNote;

import java.util.*;
import java.util.stream.Collectors;

public class JournalData {
    public static final int MAX_LOCATIONS = 128;

    private static final String TAG_RUNES = "Runes";
    private static final String TAG_LOCATIONS = "Locations";
    private static final String TAG_STRUCTURES = "Structures";
    private static final String TAG_BIOMES = "Biomes";
    private static final String TAG_DIMENSIONS = "Dimensions";
    private static final String TAG_NOTES = "Notes";

    private final UUID uuid;
    private List<Integer> runes = new ArrayList<>();

    private final List<JournalLocation> locations = new ArrayList<>();
    private final List<JournalNote> notes = new ArrayList<>();
    private final List<ResourceLocation> structures = new ArrayList<>();
    private final List<ResourceLocation> biomes = new LinkedList<>();
    private final List<ResourceLocation> dimensions = new ArrayList<>();

    private static final Map<String, JournalLocation> mappedByRuneString = new HashMap<>();

    private JournalData(Player player) {
        this.uuid = player.getUUID();
    }

    public static JournalData fromNbt(Player player, CompoundTag tag) {
        JournalData data = new JournalData(player);

        ListTag locationsNbt = tag.getList(TAG_LOCATIONS, 10);
        ListTag notesNbt = tag.getList(TAG_NOTES, 10);
        ListTag structuresNbt = tag.getList(TAG_STRUCTURES, 8);
        ListTag biomesNbt = tag.getList(TAG_BIOMES, 8);
        ListTag dimensionsNbt = tag.getList(TAG_DIMENSIONS, 8);

        data.runes = Arrays.stream(tag.getIntArray(TAG_RUNES)).boxed().collect(Collectors.toList());
        locationsNbt.forEach(compound -> data.locations.add(JournalLocation.fromNbt((CompoundTag)compound)));
        notesNbt.forEach(compound -> data.notes.add(JournalNote.fromNbt((CompoundTag)compound)));

        structuresNbt.stream().map(Tag::getAsString).map(i -> i.replace("\"", "")).forEach(
            key -> data.structures.add(new ResourceLocation(key)));

        biomesNbt.stream().map(Tag::getAsString).map(i -> i.replace("\"", "")).forEach(
            key -> data.biomes.add(new ResourceLocation(key)));

        dimensionsNbt.stream().map(Tag::getAsString).map(i -> i.replace("\"", "")).forEach(
            key -> data.dimensions.add(new ResourceLocation(key)));

        Collections.sort(data.biomes);
        Collections.sort(data.structures);
        Collections.sort(data.dimensions);
        return data;
    }

    public CompoundTag toNbt() {
        return toNbt(new CompoundTag());
    }

    public CompoundTag toNbt(CompoundTag nbt) {
        ListTag locationsNbt = new ListTag();
        ListTag notesNbt = new ListTag();
        ListTag structuresNbt = new ListTag();
        ListTag biomesNbt = new ListTag();
        ListTag dimensionsNbt = new ListTag();

        locations.forEach(location -> locationsNbt.add(location.toNbt(new CompoundTag())));
        notes.forEach(note -> notesNbt.add(note.toNbt(new CompoundTag())));
        structures.forEach(structure -> structuresNbt.add(StringTag.valueOf(structure.toString())));
        biomes.forEach(biome -> biomesNbt.add(StringTag.valueOf(biome.toString())));
        dimensions.forEach(dimension -> dimensionsNbt.add(StringTag.valueOf(dimension.toString())));

        nbt.putIntArray(TAG_RUNES, runes);
        nbt.put(TAG_LOCATIONS, locationsNbt);
        nbt.put(TAG_NOTES, notesNbt);
        nbt.put(TAG_STRUCTURES, structuresNbt);
        nbt.put(TAG_BIOMES, biomesNbt);
        nbt.put(TAG_DIMENSIONS, dimensionsNbt);

        return nbt;
    }

    public JournalLocation addLocation(Level level, BlockPos pos) {
        JournalLocation location = new JournalLocation(pos, DimensionHelper.getDimension(level));
        addLocation(location);
        return location;
    }

    public void updateLocation(JournalLocation location) {
        Optional<JournalLocation> opt = locations.stream().filter(l -> l.getId().equals(location.getId())).findFirst();
        opt.ifPresent(l -> l.populate(location));
    }

    public void deleteLocation(JournalLocation location) {
        Optional<JournalLocation> opt = locations.stream().filter(l -> l.getId().equals(location.getId())).findFirst();
        opt.ifPresent(locations::remove);
    }

    public void addDeathLocation(Level level, BlockPos pos) {
        JournalLocation location = new JournalLocation(
            new TranslatableComponent("gui.strange.journal.death_location").getString(),
            pos,
            DimensionHelper.getDimension(level),
            JournalLocation.DEFAULT_DEATH_ICON,
            null
        );
        addLocation(location);
    }

    public List<Integer> getLearnedRunes() {
        return runes;
    }

    public List<JournalLocation> getLocations() {
        return locations;
    }

    public List<ResourceLocation> getLearnedBiomes() {
        return biomes;
    }

    public List<ResourceLocation> getLearnedStructures() {
        return structures;
    }

    public List<ResourceLocation> getLearnedDimensions() {
        return dimensions;
    }

    public void learnRune(int val) {
        if (!runes.contains(val)) {
            runes.add(val);
        }
    }

    public void learnBiome(ResourceLocation biome) {
        if (!biomes.contains(biome)) {
            biomes.add(biome);
        }
    }

    public void learnStructure(ResourceLocation structure) {
        if (!structures.contains(structure)) {
            structures.add(structure);
        }
    }

    public void learnDimension(ResourceLocation dimension) {
        if (!dimensions.contains(dimension)) {
            dimensions.add(dimension);
        }
    }

    private void addLocation(JournalLocation location) {
        if (locations.size() < MAX_LOCATIONS) {
            locations.add(0, location);
            mappedByRuneString.put(location.getRunes(), location);
        }
    }

    public static Optional<JournalLocation> getLocationByRunes(String runes) {
        return Optional.ofNullable(mappedByRuneString.get(runes));
    }
}
