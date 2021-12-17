package svenhjol.strange.module.journals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeHelper;

import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class JournalBookmark {
    public static final ResourceLocation DEFAULT_ICON;
    public static final ResourceLocation DEFAULT_NETHER_ICON;
    public static final ResourceLocation DEFAULT_END_ICON;
    public static final ResourceLocation DEFAULT_DEATH_ICON;

    private static final String TAG_ID = "Id";
    private static final String TAG_RUNES = "Runes";
    private static final String TAG_POS = "Pos";
    private static final String TAG_DIM = "Dim";
    private static final String TAG_NAME = "Name";
    private static final String TAG_ICON = "Icon";
    private static final String TAG_UUID = "UUID";
    private static final String TAG_PRIVATE = "Private";

    private UUID uuid;
    private String id;
    private String name;
    private String runes;
    private BlockPos pos;
    private ResourceLocation dim;
    private ResourceLocation icon;
    private boolean isPrivate;

    private JournalBookmark() {

    }

    public JournalBookmark(UUID uuid, BlockPos pos, ResourceLocation dim) {
        this(StringHelper.tryResolveLanguageKey(Strange.MOD_ID, "gui.strange.journal.bookmark").orElse("Bookmark"), uuid, pos, dim, DEFAULT_ICON);
        setIcon(getIconForDimension(dim));
    }

    public JournalBookmark(String name, UUID uuid, BlockPos pos, ResourceLocation dim, ResourceLocation icon) {
        this(Strange.MOD_ID + "_" + RandomStringUtils.randomAlphabetic(6).toLowerCase(Locale.ROOT), uuid, name, pos, dim, icon, true);
    }

    public JournalBookmark(String id, UUID uuid, String name, BlockPos pos, ResourceLocation dim, ResourceLocation icon, boolean isPrivate) {
        this.id = id;
        this.runes = "";
        this.isPrivate = isPrivate;
        this.uuid = uuid;

        setName(name);
        setBlockPos(pos);
        setDimension(dim);
        setIcon(icon);
    }

    public static JournalBookmark fromTag(CompoundTag tag) {
        JournalBookmark bookmark = new JournalBookmark();

        bookmark.id = tag.getString(TAG_ID);
        bookmark.runes = tag.getString(TAG_RUNES);
        bookmark.name = tag.getString(TAG_NAME);
        bookmark.uuid = tag.getUUID(TAG_UUID);
        bookmark.pos = BlockPos.of(tag.getLong(TAG_POS));
        bookmark.isPrivate = tag.getBoolean(TAG_PRIVATE);
        bookmark.dim = new ResourceLocation(tag.getString(TAG_DIM));
        bookmark.icon = new ResourceLocation(tag.getString(TAG_ICON));

        return bookmark;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putString(TAG_ID, id);
        tag.putString(TAG_RUNES, runes);
        tag.putString(TAG_NAME, name);
        tag.putString(TAG_DIM, dim.toString());
        tag.putString(TAG_ICON, icon.toString());
        tag.putUUID(TAG_UUID, uuid);
        tag.putLong(TAG_POS, pos.asLong());
        tag.putBoolean(TAG_PRIVATE, isPrivate);

        return tag;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRunes() {
        return runes;
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    public ResourceLocation getDimension() {
        return dim;
    }

    public ItemStack getIcon() {
        Optional<Item> opt = DefaultedRegistry.ITEM.getOptional(icon);
        if (opt.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(opt.get());
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(ResourceLocation icon) {
        this.icon = icon;
    }

    public void setIcon(ItemStack icon) {
        this.icon = DefaultedRegistry.ITEM.getKey(icon.getItem());
    }

    public void setBlockPos(BlockPos pos) {
        this.pos = pos;

        if (this.runes.isEmpty()) {
            LogHelper.debug(getClass(), "No runes present for this bookmark yet, attempting to generate.");
            Knowledge.getKnowledgeData().ifPresent(knowledge -> {
                Optional<String> runes = KnowledgeHelper.tryGenerateUniqueId(knowledge.bookmarks, new Random(this.pos.asLong()), 0.75F, Knowledge.MIN_LENGTH, Knowledge.MAX_LENGTH);
                this.runes = runes.orElseGet(() -> knowledge.bookmarks.getStartRune() + KnowledgeHelper.generateRunesFromPos(pos));
            });
        }
    }

    public void setDimension(ResourceLocation dim) {
        this.dim = dim;
    }

    public JournalBookmark copy() {
        return new JournalBookmark(id, uuid, name, pos, dim, icon, isPrivate);
    }

    public void populate(JournalBookmark bookmark) {
        setName(bookmark.getName());
        setIcon(bookmark.getIcon());
        setBlockPos(bookmark.getBlockPos());
        setDimension(bookmark.getDimension());
    }

    private ResourceLocation getIconForDimension(ResourceLocation dimension) {
        if (Level.NETHER.location().equals(dimension)) {
            return DEFAULT_NETHER_ICON;
        } else if (Level.END.location().equals(dimension)) {
            return DEFAULT_END_ICON;
        } else {
            return DEFAULT_ICON;
        }
    }

    static {
        DEFAULT_ICON = new ResourceLocation("minecraft", "grass_block");
        DEFAULT_NETHER_ICON = new ResourceLocation("minecraft", "netherrack");
        DEFAULT_END_ICON = new ResourceLocation("minecraft", "end_stone");
        DEFAULT_DEATH_ICON = new ResourceLocation("minecraft", "skeleton_skull");
    }
}
