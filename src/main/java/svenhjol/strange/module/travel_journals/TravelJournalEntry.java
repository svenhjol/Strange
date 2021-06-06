package svenhjol.strange.module.travel_journals;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.strange.Strange;

public class TravelJournalEntry {
    public String id = "";
    public String name = "";
    public BlockPos pos;
    public ResourceLocation dim;
    public int color;

    public static final String ID = "id";
    public static final String POS = "pos";
    public static final String DIM = "dim";
    public static final String NAME = "name";
    public static final String COLOR = "color";

    public TravelJournalEntry(String id, BlockPos pos, ResourceLocation dim) {
        this.id = id;
        this.pos = pos;
        this.dim = dim;
    }

    public TravelJournalEntry(String name, BlockPos pos, ResourceLocation dim, int color) {
        this(Strange.MOD_ID + "_" + RandomStringUtils.randomAlphabetic(4), name, pos, dim, color);
    }

    public TravelJournalEntry(String id, String name, BlockPos pos, ResourceLocation dim, int color) {
        this.id = id;
        this.name = name;
        this.pos = pos;
        this.dim = dim;
        this.color = color;
    }

    public TravelJournalEntry(CompoundTag tag) {
        fromTag(tag);
    }

    public TravelJournalEntry(TravelJournalEntry entry) {
        fromEntry(entry);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString(ID, id);
        tag.putString(NAME, name.substring(0, Math.min(name.length(), TravelJournals.MAX_NAME_LENGTH)));
        tag.putInt(COLOR, color);

        if (dim != null)
            tag.putString(DIM, dim.toString());

        if (pos != null)
            tag.putLong(POS, pos.asLong());

        return tag;
    }

    public void fromTag(CompoundTag tag) {
        id = tag.getString(ID);
        name = tag.getString(NAME);
        color = tag.getInt(COLOR);

        if (tag.contains(POS))
            pos = BlockPos.of(tag.getLong(POS));

        if (tag.contains(DIM))
            dim = new ResourceLocation(tag.getString(DIM));
    }

    public void fromEntry(TravelJournalEntry entry) {
        id = entry.id;
        name = entry.name;
        pos = entry.pos;
        dim = entry.dim;
        color = entry.color;
    }

    public TravelJournalEntry copy() {
        return new TravelJournalEntry(this.id, this.name, this.pos, this.dim, this.color);
    }
}
