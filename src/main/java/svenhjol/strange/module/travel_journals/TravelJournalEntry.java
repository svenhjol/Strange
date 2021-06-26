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

    public static final String ID_NBT = "id";
    public static final String POS_NBT = "pos";
    public static final String DIM_NBT = "dim";
    public static final String NAME_NBT = "name";
    public static final String COLOR_NBT = "color";

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
        fromNbt(tag);
    }

    public TravelJournalEntry(TravelJournalEntry entry) {
        fromEntry(entry);
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString(ID_NBT, id);
        nbt.putString(NAME_NBT, name.substring(0, Math.min(name.length(), TravelJournals.MAX_NAME_LENGTH)));
        nbt.putInt(COLOR_NBT, color);

        if (dim != null)
            nbt.putString(DIM_NBT, dim.toString());

        if (pos != null)
            nbt.putLong(POS_NBT, pos.asLong());

        return nbt;
    }

    public void fromNbt(CompoundTag nbt) {
        id = nbt.getString(ID_NBT);
        name = nbt.getString(NAME_NBT);
        color = nbt.getInt(COLOR_NBT);

        if (nbt.contains(POS_NBT))
            pos = BlockPos.of(nbt.getLong(POS_NBT));

        if (nbt.contains(DIM_NBT))
            dim = new ResourceLocation(nbt.getString(DIM_NBT));
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
