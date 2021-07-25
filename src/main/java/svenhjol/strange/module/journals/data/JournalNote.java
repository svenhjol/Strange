package svenhjol.strange.module.journals.data;

import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.strange.Strange;

public class JournalNote {
    private static final String TAG_ID = "Id";
    private static final String TAG_NAME = "Name";
    private static final String TAG_NOTE = "Note";

    private String id = "";
    private String name = "";
    private String note = "";

    public JournalNote(String name, String note) {
        this(Strange.MOD_ID + "_" + RandomStringUtils.randomAlphabetic(4), name, note);
    }

    public JournalNote(String id, String name, String note) {
        this.id = id;
        this.name = name;
        this.note = note;
    }

    public static JournalNote fromNbt(CompoundTag nbt) {
        String id = nbt.getString(TAG_ID);
        String name = nbt.getString(TAG_NAME);
        String note = nbt.getString(TAG_NOTE);

        return new JournalNote(id, name, note);
    }

    public CompoundTag toNbt(CompoundTag nbt) {
        return nbt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNote() {
        return note;
    }
}
