package svenhjol.strange.module.knowledge;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Destination {
    public static final String TAG_RUNES = "Runes";
    public static final String TAG_POSITION = "Pos";
    public static final String TAG_DIMENSION = "Dim";
    public static final String TAG_LOCATION = "Loc";
    public static final String TAG_ITEMS = "Items";
    public static final String TAG_PLAYER = "Player";
    public static final String TAG_DIFFICULTY = "Diff";
    public static final String TAG_DECAY = "Decay";

    public String runes;
    public BlockPos pos;
    public ResourceLocation dimension;
    public ResourceLocation location;
    public List<ItemStack> items = new ArrayList<>();
    public String player;
    public float difficulty;
    public float decay;

    public Destination(String runes) {
        this.runes = runes;
    }

    public String getId() {
        return this.runes;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putFloat(TAG_DIFFICULTY, difficulty);
        tag.putFloat(TAG_DECAY, decay);

        if (this.items != null && !this.items.isEmpty()) {
            ListTag itemsNbt = new ListTag();
            this.items.forEach(item -> {
                CompoundTag itemTag = new CompoundTag();
                item.save(itemTag);
                itemsNbt.add(itemTag);
            });
            tag.put(TAG_ITEMS, itemsNbt);
        }

        if (runes != null) {
            tag.putString(TAG_RUNES, runes);
        }

        if (pos != null) {
            tag.putLong(TAG_POSITION, pos.asLong());
        }

        if (dimension != null) {
            tag.putString(TAG_DIMENSION, dimension.toString());
        }

        if (location != null) {
            tag.putString(TAG_LOCATION, location.toString());
        }

        if (player != null) {
            tag.putString(TAG_PLAYER, player);
        }

        return tag;
    }

    public static Destination fromTag(CompoundTag tag) {
        String runes = tag.getString(TAG_RUNES);
        Destination destination = new Destination(runes);

        destination.pos = BlockPos.of(tag.getLong(TAG_POSITION));
        destination.dimension = new ResourceLocation(tag.getString(TAG_DIMENSION));
        destination.location = new ResourceLocation(tag.getString(TAG_LOCATION));
        destination.player = tag.getString(TAG_PLAYER);
        destination.difficulty = tag.getFloat(TAG_DIFFICULTY);
        destination.decay = tag.getFloat(TAG_DECAY);

        ListTag itemsList = tag.getList(TAG_ITEMS, 10);
        for (int i = 0; i < itemsList.size(); i++) {
            CompoundTag itemTag = itemsList.getCompound(i);
            ItemStack stack = ItemStack.of(itemTag);
            destination.items.add(stack);
        }

        return destination;
    }
}
