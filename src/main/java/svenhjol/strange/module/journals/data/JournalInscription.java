package svenhjol.strange.module.journals.data;

import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.Strange;

public class JournalInscription {
    private static final String TAG_ID = "Id";
    private static final String TAG_NAME = "Name";
    private static final String TAG_RUNES = "Runes";
    private static final String TAG_NOTE_ID = "Note";

    private String id = "";
    private String name = "";
    private String runes = "";
    private String noteId = "";

    public JournalInscription(String name, String runes, @Nullable String noteId) {
        this(Strange.MOD_ID + "_" + RandomStringUtils.randomAlphabetic(4), name, runes, noteId);
    }

    public JournalInscription(String id, String name, String runes, @Nullable String noteId) {
        this.id = id;
        this.name = name;
        this.runes = runes;
        this.noteId = noteId;
    }

    public static JournalInscription fromNbt(CompoundTag nbt) {
        String id = nbt.getString(TAG_ID);
        String name = nbt.getString(TAG_NAME);
        String runes = nbt.getString(TAG_RUNES);
        String noteId = nbt.getString(TAG_NOTE_ID);

        return new JournalInscription(id, name, runes, noteId);
    }

    public CompoundTag toNbt(CompoundTag nbt) {
        return nbt;
    }

    public String getId() {
        return id;
    }
}
