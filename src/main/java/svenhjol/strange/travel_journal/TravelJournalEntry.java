package svenhjol.strange.travel_journal;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.strange.Strange;
import svenhjol.strange.module.TravelJournals;

public class TravelJournalEntry {
    public String id = "";
    public String name = "";
    public BlockPos pos;
    public Identifier dim;
    public int color;

    public static final String ID = "id";
    public static final String POS = "pos";
    public static final String DIM = "dim";
    public static final String NAME = "name";
    public static final String COLOR = "color";

    public TravelJournalEntry(String id, BlockPos pos, Identifier dim) {
        this.id = id;
        this.pos = pos;
        this.dim = dim;
    }

    public TravelJournalEntry(String name, BlockPos pos, Identifier dim, int color) {
        this(Strange.MOD_ID + "_" + RandomStringUtils.randomAlphabetic(4), name, pos, dim, color);
    }

    public TravelJournalEntry(String id, String name, BlockPos pos, Identifier dim, int color) {
        this.id = id;
        this.name = name;
        this.pos = pos;
        this.dim = dim;
        this.color = color;
    }

    public TravelJournalEntry(NbtCompound tag) {
        fromTag(tag);
    }

    public TravelJournalEntry(TravelJournalEntry entry) {
        fromEntry(entry);
    }

    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString(ID, id);
        tag.putString(NAME, name.substring(0, Math.min(name.length(), TravelJournals.MAX_NAME_LENGTH)));
        tag.putInt(COLOR, color);

        if (dim != null)
            tag.putString(DIM, dim.toString());

        if (pos != null)
            tag.putLong(POS, pos.asLong());

        return tag;
    }

    public void fromTag(NbtCompound tag) {
        id = tag.getString(ID);
        name = tag.getString(NAME);
        color = tag.getInt(COLOR);

        if (tag.contains(POS))
            pos = BlockPos.fromLong(tag.getLong(POS));

        if (tag.contains(DIM))
            dim = new Identifier(tag.getString(DIM));
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
