package svenhjol.strange.feature.travel_journal;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import svenhjol.charmony.helper.TextHelper;

public class Bookmark {
    public static final String ID_TAG = "id";
    public static final String NAME_TAG = "name";
    public static final String DIM_TAG = "dim";
    public static final String POS_TAG = "pos";
    public static final String ITEM_TAG = "item";

    public String id;
    public String name;
    public BlockPos pos;
    public ResourceLocation dim;
    public ResourceLocation item;

    public Bookmark(String name, BlockPos pos, ResourceLocation dim) {
        this(TravelJournalHelper.randomId(), name, pos, dim, TravelJournalHelper.defaultItem());
    }

    public Bookmark(String id, String name, BlockPos pos, ResourceLocation dim,
                    ResourceLocation item) {
        this.id = id;
        this.name = name;
        this.pos = pos;
        this.dim = dim;
        this.item = item;
    }

    private Bookmark() {}

    public CompoundTag save() {
        var tag = new CompoundTag();

        tag.putString(ID_TAG, id);
        tag.putString(NAME_TAG, name);
        tag.putLong(POS_TAG, pos.asLong());
        tag.putString(DIM_TAG, dim.toString());
        tag.putString(ITEM_TAG, item.toString());

        return tag;
    }

    public static Bookmark load(CompoundTag tag) {
        var bookmark = new Bookmark();

        bookmark.id = tag.getString(ID_TAG);
        bookmark.name = tag.getString(NAME_TAG);
        bookmark.pos = BlockPos.of(tag.getLong(POS_TAG));
        bookmark.dim = ResourceLocation.tryParse(tag.getString(DIM_TAG));
        bookmark.item = ResourceLocation.tryParse(tag.getString(ITEM_TAG));

        return bookmark;
    }

    public static Bookmark playerDefault(Player player) {
        var biomeName = TextHelper.translatable(TravelJournalHelper.getPlayerBiomeLocaleKey(player));
        var bookmarkName = TextHelper.translatable(
            "gui.strange.travel_journal.bookmarks.default_name", biomeName).getString();

        var dimension = TravelJournalHelper.getPlayerDimension(player);
        return new Bookmark(bookmarkName, player.blockPosition(), dimension);
    }

}