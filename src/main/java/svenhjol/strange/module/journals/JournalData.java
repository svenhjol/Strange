package svenhjol.strange.module.journals;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeData;

import java.util.*;
import java.util.stream.Collectors;

public class JournalData {
    private static final String TAG_RUNES = "Runes";
    private static final String TAG_BOOKMARKS = "Bookmarks";
    private static final String TAG_STRUCTURES = "Structures";
    private static final String TAG_BIOMES = "Biomes";
    private static final String TAG_DIMENSIONS = "Dimensions";

    private List<Integer> runes = new ArrayList<>();

    private final List<ResourceLocation> structures = new ArrayList<>();
    private final List<ResourceLocation> biomes = new LinkedList<>();
    private final List<ResourceLocation> dimensions = new ArrayList<>();

    private final UUID uuid;

    private JournalData(Player player) {
        this.uuid = player.getUUID();
    }

    public static JournalData fromNbt(Player player, CompoundTag tag) {
        JournalData data = new JournalData(player);

        ListTag structuresNbt = tag.getList(TAG_STRUCTURES, 8);
        ListTag biomesNbt = tag.getList(TAG_BIOMES, 8);
        ListTag dimensionsNbt = tag.getList(TAG_DIMENSIONS, 8);

        data.runes = Arrays.stream(tag.getIntArray(TAG_RUNES)).boxed().collect(Collectors.toList());

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
        ListTag bookmarksNbt = new ListTag();
        ListTag structuresNbt = new ListTag();
        ListTag biomesNbt = new ListTag();
        ListTag dimensionsNbt = new ListTag();

        structures.forEach(structure -> structuresNbt.add(StringTag.valueOf(structure.toString())));
        biomes.forEach(biome -> biomesNbt.add(StringTag.valueOf(biome.toString())));
        dimensions.forEach(dimension -> dimensionsNbt.add(StringTag.valueOf(dimension.toString())));

        nbt.putIntArray(TAG_RUNES, runes);
        nbt.put(TAG_BOOKMARKS, bookmarksNbt);
        nbt.put(TAG_STRUCTURES, structuresNbt);
        nbt.put(TAG_BIOMES, biomesNbt);
        nbt.put(TAG_DIMENSIONS, dimensionsNbt);

        return nbt;
    }

    public JournalBookmark addBookmark(Player player) {
        JournalBookmark bookmark = new JournalBookmark(player.getUUID(), player.blockPosition(), DimensionHelper.getDimension(player.level));
        addBookmark(bookmark);
        return bookmark;
    }

    public void updateBookmark(JournalBookmark bookmark) {
        Knowledge.getKnowledgeData().ifPresent(knowledge -> {
            Optional<JournalBookmark> opt = knowledge.bookmarks.values().stream().filter(b -> b.getId().equals(bookmark.getId())).findFirst();
            opt.ifPresent(b -> b.populate(bookmark));
            knowledge.setDirty();
        });
    }

    public void deleteBookmark(JournalBookmark bookmark) {
        Knowledge.getKnowledgeData().ifPresent(knowledge -> {
            Optional<JournalBookmark> opt = knowledge.bookmarks.values().stream().filter(b -> b.getId().equals(bookmark.getId())).findFirst();
            opt.ifPresent(b -> knowledge.bookmarks.remove(b.getRunes()));
        });
    }

    public void addDeathBookmark(Player player) {
        JournalBookmark bookmark = new JournalBookmark(
            new TranslatableComponent("gui.strange.journal.death_bookmark").getString(),
            player.getUUID(),
            player.blockPosition(),
            DimensionHelper.getDimension(player.getLevel()),
            JournalBookmark.DEFAULT_DEATH_ICON
        );
        addBookmark(bookmark);
    }

    public Optional<JournalBookmark> getBookmark(String runes) {
        return getBookmarks().stream().filter(b -> b.getRunes().equals(runes)).findFirst();
    }

    public List<Integer> getLearnedRunes() {
        return runes;
    }

    public List<JournalBookmark> getBookmarks() {
        KnowledgeData knowledge = Knowledge.getKnowledgeData().orElseThrow();
        return knowledge.bookmarks.values().stream().filter(b -> b.getUuid().equals(uuid) || !b.isPrivate()).collect(Collectors.toList());
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

    private void addBookmark(JournalBookmark bookmark) {
        Knowledge.getKnowledgeData().ifPresent(knowledge -> knowledge.bookmarks.register(bookmark));
    }
}
