package svenhjol.strange.traveljournals;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class JournalEntry {
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

    public JournalEntry(String id, BlockPos pos, Identifier dim) {
        this.id = id;
        this.pos = pos;
        this.dim = dim;
    }

    public JournalEntry(String id, String name, BlockPos pos, Identifier dim, int color) {
        this.id = id;
        this.name = name;
        this.pos = pos;
        this.dim = dim;
        this.color = color;
    }

    public JournalEntry(CompoundTag tag) {
        fromTag(tag);
    }

    public JournalEntry(JournalEntry entry) {
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
            pos = BlockPos.fromLong(tag.getLong(POS));

        if (tag.contains(DIM))
            dim = new Identifier(tag.getString(DIM));
    }

    public void fromEntry(JournalEntry entry) {
        id = entry.id;
        name = entry.name;
        pos = entry.pos;
        dim = entry.dim;
        color = entry.color;
    }

    public JournalEntry copy() {
        return new JournalEntry(this.id, this.name, this.pos, this.dim, this.color);
    }
}
