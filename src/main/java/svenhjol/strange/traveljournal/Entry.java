package svenhjol.strange.traveljournal;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.StringUtils;

public class Entry {
    public String id = "";
    public String name = "";
    public String posref = "";
    public BlockPos pos;
    public int dim;
    public int color;

    public static final String ID = "id";
    public static final String POS = "pos";
    public static final String DIM = "dim";
    public static final String NAME = "name";
    public static final String COLOR = "color";
    public static final String POSREF = "posref";

    public Entry(String id, BlockPos pos, int dim) {
        this.id = id;
        this.pos = pos;
        this.dim = dim;
        this.posref = calculatePosRef(pos, dim);
    }

    public Entry(String id, String name, BlockPos pos, int dim, int color) {
        this.id = id;
        this.name = name;
        this.pos = pos;
        this.dim = dim;
        this.color = color;
        this.posref = calculatePosRef(pos, dim);
    }

    public Entry(CompoundNBT nbt) {
        fromNBT(nbt);
    }

    public Entry(Entry entry) {
        fromEntry(entry);
    }

    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString(ID, id);
        nbt.putString(NAME, name);
        nbt.putString(POSREF, posref);
        nbt.putLong(POS, pos.toLong());
        nbt.putInt(DIM, dim);
        nbt.putInt(COLOR, color);
        return nbt;
    }

    public void fromNBT(CompoundNBT nbt) {
        long posLong = nbt.getLong(POS);
        id = nbt.getString(ID);
        name = nbt.getString(NAME);
        pos = posLong != 0 ? BlockPos.fromLong(posLong) : null;
        dim = nbt.getInt(DIM);
        color = nbt.getInt(COLOR);
        posref = nbt.getString(POSREF);
    }

    public void fromEntry(Entry entry) {
        id = entry.id;
        name = entry.name;
        pos = entry.pos;
        dim = entry.dim;
        color = entry.color;
        posref = entry.posref;
    }

    private String calculatePosRef(BlockPos pos, int dim) {
        long l = pos.toLong() + (long) dim;
        String encoded = Long.toString(l, 26);
        encoded = encoded.replace('-', '0');
        encoded = StringUtils.leftPad(encoded, 12, "0");
        String trimmed = encoded.substring(encoded.length() - 12);
        return trimmed;
    }
}
