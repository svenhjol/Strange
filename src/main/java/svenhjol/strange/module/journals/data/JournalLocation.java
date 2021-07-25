package svenhjol.strange.module.journals.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.Strange;

public class JournalLocation {
    private static final int DEFAULT_COLOR = 15;

    private static final String TAG_ID = "Id";
    private static final String TAG_POS = "Pos";
    private static final String TAG_DIM = "Dim";
    private static final String TAG_NAME = "Name";
    private static final String TAG_COLOR = "Color";
    private static final String TAG_NOTE_ID = "Note";

    private String id = "";
    private String name = "";
    private String noteId = "";
    private BlockPos pos;
    private ResourceLocation dim;
    private int color;

    public JournalLocation(BlockPos pos, ResourceLocation dim) {
        this(new TranslatableComponent("gui.strange.journal.new_location").getString(), pos, dim, DEFAULT_COLOR, null);
    }

    public JournalLocation(String name, BlockPos pos, ResourceLocation dim, int color, @Nullable String noteId) {
        this(Strange.MOD_ID + "_" + RandomStringUtils.randomAlphabetic(4), name, pos, dim, color, noteId);
    }

    public JournalLocation(String id, String name, BlockPos pos, ResourceLocation dim, int color, @Nullable String noteId) {
        this.id = id;
        this.name = name;
        this.noteId = noteId;
        this.pos = pos;
        this.dim = dim;
        this.color = color;
    }

    public static JournalLocation fromNbt(CompoundTag nbt) {
        String id = nbt.getString(TAG_ID);
        String name = nbt.getString(TAG_NAME);
        String noteId = nbt.getString(TAG_NOTE_ID);
        BlockPos pos = BlockPos.of(nbt.getLong(TAG_POS));
        ResourceLocation dim = new ResourceLocation(nbt.getString(TAG_DIM));
        int color = nbt.getInt(TAG_COLOR);

        return new JournalLocation(id, name, pos, dim, color, noteId);
    }

    public CompoundTag toNbt(CompoundTag nbt) {
        return nbt;
    }

    public String getId() {
        return id;
    }
}
