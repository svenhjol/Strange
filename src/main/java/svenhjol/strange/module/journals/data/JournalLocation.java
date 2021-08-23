package svenhjol.strange.module.journals.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.Strange;

import java.util.Locale;
import java.util.Optional;

public class JournalLocation {
    public static final ResourceLocation DEFAULT_ICON = new ResourceLocation("minecraft", "grass_block");
    public static final ResourceLocation DEFAULT_DEATH_ICON = new ResourceLocation("minecraft", "skeleton_skull");

    private static final String TAG_ID = "Id";
    private static final String TAG_POS = "Pos";
    private static final String TAG_DIM = "Dim";
    private static final String TAG_NAME = "Name";
    private static final String TAG_ICON = "Icon";
    private static final String TAG_NOTE_ID = "Note";

    private String id = "";
    private String name = "";
    private String noteId = "";
    private BlockPos pos;
    private ResourceLocation dim;
    private ResourceLocation icon;

    public JournalLocation(BlockPos pos, ResourceLocation dim) {
        this(new TranslatableComponent("gui.strange.journal.new_location").getString(), pos, dim, DEFAULT_ICON, null);
    }

    public JournalLocation(String name, BlockPos pos, ResourceLocation dim, ResourceLocation icon, @Nullable String noteId) {
        this(Strange.MOD_ID + "_" + RandomStringUtils.randomAlphabetic(6).toLowerCase(Locale.ROOT), name, pos, dim, icon, noteId);
    }

    public JournalLocation(String id, String name, BlockPos pos, ResourceLocation dim, ResourceLocation icon, @Nullable String noteId) {
        this.id = id;
        this.name = name;
        this.noteId = noteId != null ? noteId : "";
        this.pos = pos;
        this.dim = dim;
        this.icon = icon;
    }

    public static JournalLocation fromNbt(CompoundTag nbt) {
        String id = nbt.getString(TAG_ID);
        String name = nbt.getString(TAG_NAME);
        String noteId = nbt.getString(TAG_NOTE_ID);
        BlockPos pos = BlockPos.of(nbt.getLong(TAG_POS));
        ResourceLocation dim = new ResourceLocation(nbt.getString(TAG_DIM));
        ResourceLocation icon = new ResourceLocation(nbt.getString(TAG_ICON));

        return new JournalLocation(id, name, pos, dim, icon, noteId);
    }

    public CompoundTag toNbt(CompoundTag nbt) {
        nbt.putString(TAG_ID, id);
        nbt.putString(TAG_NAME, name);
        nbt.putString(TAG_NOTE_ID, noteId != null ? noteId : "");
        nbt.putString(TAG_DIM, dim.toString());
        nbt.putString(TAG_ICON, icon.toString());
        nbt.putLong(TAG_POS, pos.asLong());

        return nbt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    public ResourceLocation getDimension() {
        return dim;
    }

    public ItemStack getIcon() {
        Optional<Item> opt = DefaultedRegistry.ITEM.getOptional(icon);
        if (opt.isEmpty())
            return ItemStack.EMPTY;

        return new ItemStack(opt.get());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(ItemStack icon) {
        this.icon = DefaultedRegistry.ITEM.getKey(icon.getItem());
    }

    public void setBlockPos(BlockPos pos) {
        this.pos = pos;
    }

    public void setDimension(ResourceLocation dim) {
        this.dim = dim;
    }

    public JournalLocation copy() {
        return new JournalLocation(id, name, pos, dim, icon, noteId);
    }

    public void populate(JournalLocation location) {
        setName(location.getName());
        setIcon(location.getIcon());
        setBlockPos(location.getBlockPos());
        setDimension(location.getDimension());
    }
}
