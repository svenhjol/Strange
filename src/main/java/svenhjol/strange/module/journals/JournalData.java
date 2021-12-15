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

import java.util.*;
import java.util.stream.Collectors;

public class JournalData {
    public static final int MAX_BOOKMARKS = 128;

    private static final String TAG_RUNES = "Runes";
    private static final String TAG_BOOKMARKS = "Bookmarks";
    private static final String TAG_STRUCTURES = "Structures";
    private static final String TAG_BIOMES = "Biomes";
    private static final String TAG_DIMENSIONS = "Dimensions";

    private final UUID uuid;
    private List<Integer> runes = new ArrayList<>();

    private final List<JournalBookmark> bookmarks = new ArrayList<>();
    private final List<ResourceLocation> structures = new ArrayList<>();
    private final List<ResourceLocation> biomes = new LinkedList<>();
    private final List<ResourceLocation> dimensions = new ArrayList<>();

    private static final Map<String, JournalBookmark> mappedByRuneString = new HashMap<>();

    private JournalData(Player player) {
        this.uuid = player.getUUID();
    }

    public static JournalData fromNbt(Player player, CompoundTag tag) {
        JournalData data = new JournalData(player);

        ListTag bookmarksNbt = tag.getList(TAG_BOOKMARKS, 10);
        ListTag structuresNbt = tag.getList(TAG_STRUCTURES, 8);
        ListTag biomesNbt = tag.getList(TAG_BIOMES, 8);
        ListTag dimensionsNbt = tag.getList(TAG_DIMENSIONS, 8);

        data.runes = Arrays.stream(tag.getIntArray(TAG_RUNES)).boxed().collect(Collectors.toList());
        bookmarksNbt.forEach(compound -> data.bookmarks.add(JournalBookmark.fromNbt((CompoundTag)compound)));

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

        bookmarks.forEach(bookmark -> bookmarksNbt.add(bookmark.toNbt(new CompoundTag())));
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

    public JournalBookmark addBookmark(Level level, BlockPos pos) {
        JournalBookmark bookmark = new JournalBookmark(pos, DimensionHelper.getDimension(level));
        addBookmark(bookmark);
        return bookmark;
    }

    public void updateBookmark(JournalBookmark bookmark) {
        Optional<JournalBookmark> opt = bookmarks.stream().filter(l -> l.getId().equals(bookmark.getId())).findFirst();
        opt.ifPresent(l -> l.populate(bookmark));
    }

    public void deleteBookmark(JournalBookmark bookmark) {
        Optional<JournalBookmark> opt = bookmarks.stream().filter(l -> l.getId().equals(bookmark.getId())).findFirst();
        opt.ifPresent(bookmarks::remove);
    }

    public void addDeathBookmark(Level level, BlockPos pos) {
        JournalBookmark bookmark = new JournalBookmark(
            new TranslatableComponent("gui.strange.journal.death_bookmark").getString(),
            pos,
            DimensionHelper.getDimension(level),
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
        return bookmarks;
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
        if (bookmarks.size() < MAX_BOOKMARKS) {
            bookmarks.add(0, bookmark);
            mappedByRuneString.put(bookmark.getRunes(), bookmark);
        }
    }

    public static Optional<JournalBookmark> getBookmarkByRunes(String runes) {
        return Optional.ofNullable(mappedByRuneString.get(runes));
    }
}
