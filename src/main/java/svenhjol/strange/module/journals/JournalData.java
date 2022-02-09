package svenhjol.strange.module.journals;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.module.discoveries.Discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JournalData {
    private static final String RUNES_TAG = "Runes";
    private static final String BIOMES_TAG = "Biomes";
    private static final String DIMENSIONS_TAG = "Dimensions";
    private static final String STRUCTURES_TAG = "Structures";
    private static final String DISCOVERIES_TAG = "Discoveries";
    private static final String IGNORED_DISCOVERIES_TAG = "IgnoredDiscoveries";
    private static final String OPENED_JOURNAL_TAG = "OpenedJournal";

    private final List<ResourceLocation> biomes = new ArrayList<>();
    private final List<ResourceLocation> dimensions = new ArrayList<>();
    private final List<ResourceLocation> structures = new ArrayList<>();
    private final List<String> discoveries = new ArrayList<>();
    private final List<String> ignoredDiscoveries = new ArrayList<>();
    private boolean openedJournal = false;

    private List<Integer> runes = new ArrayList<>();

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        ListTag biomesTag = new ListTag();
        ListTag dimensionsTag = new ListTag();
        ListTag structuresTag = new ListTag();
        ListTag discoveriesTag = new ListTag();
        ListTag ignoredDiscoveriesTag = new ListTag();

        biomes.forEach(biome -> biomesTag.add(StringTag.valueOf(biome.toString())));
        dimensions.forEach(dimension -> dimensionsTag.add(StringTag.valueOf(dimension.toString())));
        structures.forEach(structure -> structuresTag.add(StringTag.valueOf(structure.toString())));
        discoveries.forEach(runes -> discoveriesTag.add(StringTag.valueOf(runes)));
        ignoredDiscoveries.forEach(runes -> ignoredDiscoveriesTag.add(StringTag.valueOf(runes)));

        tag.putBoolean(OPENED_JOURNAL_TAG, openedJournal);
        tag.putIntArray(RUNES_TAG, runes);
        tag.put(BIOMES_TAG, biomesTag);
        tag.put(DIMENSIONS_TAG, dimensionsTag);
        tag.put(STRUCTURES_TAG, structuresTag);
        tag.put(DISCOVERIES_TAG, discoveriesTag);
        tag.put(IGNORED_DISCOVERIES_TAG, ignoredDiscoveriesTag);

        return tag;
    }

    public boolean hasOpened() {
        return openedJournal;
    }

    public List<Integer> getLearnedRunes() {
        return runes;
    }

    public List<ResourceLocation> getLearnedBiomes() {
        return biomes;
    }

    public List<ResourceLocation> getLearnedDimensions() {
        return dimensions;
    }

    public List<ResourceLocation> getLearnedStructures() {
        return structures;
    }

    public List<String> getDiscoveries() {
        return discoveries;
    }

    public List<String> getIgnoredDiscoveries() {
        return ignoredDiscoveries;
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

    public void learnDimension(ResourceLocation dimension) {
        if (!dimensions.contains(dimension)) {
            dimensions.add(dimension);
        }
    }

    public void learnStructure(ResourceLocation structure) {
        if (!structures.contains(structure)) {
            structures.add(structure);
        }
    }

    public void learnDiscovery(Discovery discovery) {
        var runes = discovery.getRunes();
        if (!discoveries.contains(runes)) {
            discoveries.add(runes);
        }
    }

    public void ignoreDiscovery(Discovery discovery) {
        var runes = discovery.getRunes();
        if (!ignoredDiscoveries.contains(runes)) {
            ignoredDiscoveries.add(runes);
        }
    }

    public void unignoreDiscovery(Discovery discovery) {
        var runes = discovery.getRunes();
        ignoredDiscoveries.remove(runes);
    }

    public void setOpenedJournal(boolean val) {
        openedJournal = val;
    }

    public static JournalData load(CompoundTag tag) {
        JournalData journal = new JournalData();

        journal.openedJournal = tag.getBoolean(OPENED_JOURNAL_TAG);
        ListTag biomesTag = tag.getList(BIOMES_TAG, 8);
        ListTag dimensionsTag = tag.getList(DIMENSIONS_TAG, 8);
        ListTag structuresTag = tag.getList(STRUCTURES_TAG, 8);
        ListTag discoveriesTag = tag.getList(DISCOVERIES_TAG, 8);
        ListTag ignoredDiscoveriesTag = tag.getList(IGNORED_DISCOVERIES_TAG, 8);

        journal.runes = Arrays.stream(tag.getIntArray(RUNES_TAG)).boxed().collect(Collectors.toList());

        biomesTag.stream()
            .map(Tag::getAsString)
            .map(s -> s.replace("\"", ""))
            .forEach(t -> journal.biomes.add(new ResourceLocation(t)));

        dimensionsTag.stream()
            .map(Tag::getAsString)
            .map(s -> s.replace("\"", ""))
            .forEach(t -> journal.dimensions.add(new ResourceLocation(t)));

        structuresTag.stream()
            .map(Tag::getAsString)
            .map(s -> s.replace("\"", ""))
            .forEach(t -> journal.structures.add(new ResourceLocation(t)));

        discoveriesTag.stream()
            .map(Tag::getAsString)
            .forEach(journal.discoveries::add);

        ignoredDiscoveriesTag.stream()
            .map(Tag::getAsString)
            .forEach(journal.ignoredDiscoveries::add);

        Collections.sort(journal.biomes);
        Collections.sort(journal.dimensions);
        Collections.sort(journal.structures);

        return journal;
    }
}
