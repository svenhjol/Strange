package svenhjol.strange.module.bookmarks;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.Strange;

import java.util.UUID;

public class Bookmark {
    public static final String RUNES_TAG = "Runes";
    public static final String POS_TAG = "Pos";
    public static final String DIM_TAG = "Dim";
    public static final String NAME_TAG = "Name";
    public static final String ICON_TAG = "Icon";
    public static final String UUID_TAG = "UUID";
    public static final String PRIVATE_TAG = "Private";

    private UUID uuid;
    private String name;
    private String runes;
    private BlockPos pos;
    private ResourceLocation dim;
    private ResourceLocation icon;
    private boolean isPrivate;

    private Bookmark() {}

    public Bookmark(UUID uuid, BlockPos pos, ResourceLocation dim) {
        this(
            uuid,
            StringHelper.tryResolveLanguageKey(Strange.MOD_ID, "gui.strange.journal.bookmark").orElse("Bookmark"),
            pos,
            dim,
            DefaultIcon.OVERWORLD.getId()
        );
        this.icon = DefaultIcon.forDimension(dim).getId();
    }

    public Bookmark(UUID uuid, String name, BlockPos pos, ResourceLocation dim, ResourceLocation icon) {
        this.uuid = uuid;
        this.name = name;
        this.pos = pos;
        this.dim = dim;
        this.icon = icon;
        this.isPrivate = true;
        this.runes = "";
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID(UUID_TAG, uuid);
        tag.putString(RUNES_TAG, runes);
        tag.putString(NAME_TAG, name);
        tag.putString(DIM_TAG, dim.toString());
        tag.putString(ICON_TAG, icon.toString());
        tag.putLong(POS_TAG, pos.asLong());
        tag.putBoolean(PRIVATE_TAG, isPrivate);

        return tag;
    }

    /**
     * Required when editing so we don't change an existing bookmark by reference.
     */
    public Bookmark copy() {
        var tag = save();
        return load(tag);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBlockPos(BlockPos pos) {
        this.pos = pos;
    }

    public void setDimension(ResourceLocation dim) {
        this.dim = dim;
    }

    public void setIcon(ResourceLocation icon) {
        this.icon = icon;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void setRunes(String runes) {
        this.runes = runes;
    }

    public String getName() {
        return name;
    }

    public String getRunes() {
        return runes;
    }

    public UUID getUuid() {
        return uuid;
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    public ResourceLocation getDimension() {
        return dim;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public static Bookmark load(CompoundTag tag) {
        Bookmark bookmark = new Bookmark();

        bookmark.uuid = tag.getUUID(UUID_TAG);
        bookmark.runes = tag.getString(RUNES_TAG);
        bookmark.name = tag.getString(NAME_TAG);
        bookmark.pos = BlockPos.of(tag.getLong(POS_TAG));
        bookmark.dim = new ResourceLocation(tag.getString(DIM_TAG));
        bookmark.icon = new ResourceLocation(tag.getString(ICON_TAG));
        bookmark.isPrivate = tag.getBoolean(PRIVATE_TAG);

        return bookmark;
    }
}
