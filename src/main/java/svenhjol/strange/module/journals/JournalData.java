package svenhjol.strange.module.journals;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

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

    private final List<ResourceLocation> biomes = new ArrayList<>();
    private final List<ResourceLocation> dimensions = new ArrayList<>();
    private final List<ResourceLocation> structures = new ArrayList<>();

    private List<Integer> runes = new ArrayList<>();

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        ListTag biomesTag = new ListTag();
        ListTag dimensionsTag = new ListTag();
        ListTag structuresTag = new ListTag();

        biomes.forEach(biome -> biomesTag.add(StringTag.valueOf(biome.toString())));
        dimensions.forEach(dimension -> dimensionsTag.add(StringTag.valueOf(dimension.toString())));
        structures.forEach(structure -> structuresTag.add(StringTag.valueOf(structure.toString())));

        tag.putIntArray(RUNES_TAG, runes);
        tag.put(BIOMES_TAG, biomesTag);
        tag.put(DIMENSIONS_TAG, dimensionsTag);
        tag.put(STRUCTURES_TAG, structuresTag);

        return tag;
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

    public static JournalData load(CompoundTag tag) {
        JournalData journal = new JournalData();

        ListTag biomesTag = tag.getList(BIOMES_TAG, 8);
        ListTag dimensionsTag = tag.getList(DIMENSIONS_TAG, 8);
        ListTag structuresTag = tag.getList(STRUCTURES_TAG, 8);

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

        Collections.sort(journal.biomes);
        Collections.sort(journal.dimensions);
        Collections.sort(journal.structures);

        return journal;
    }
}
