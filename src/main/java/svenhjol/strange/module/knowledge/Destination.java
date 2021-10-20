package svenhjol.strange.module.knowledge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class Destination {
    public static final String TAG_RUNES = "Runes";
    public static final String TAG_POSITION = "Pos";
    public static final String TAG_DIMENSION = "Dim";
    public static final String TAG_LOCATION = "Loc";
    public static final String TAG_ITEMS = "Items";
    public static final String TAG_PLAYER = "Player";
    public static final String TAG_CLUE = "Clue";
    public static final String TAG_DIFFICULTY = "Diff";
    public static final String TAG_DECAY = "Decay";

    public String runes;
    public BlockPos pos;
    public ResourceLocation dimension;
    public ResourceLocation location;
    public List<Item> items = new ArrayList<>();
    public String clue;
    public String player;
    public float difficulty;
    public float decay;

    public Destination(String runes) {
        this.runes = runes;
    }

    public String getRunes() {
        return this.runes;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putFloat(TAG_DIFFICULTY, difficulty);
        tag.putFloat(TAG_DECAY, decay);

        if (this.items != null && !this.items.isEmpty()) {
            ListTag itemsNbt = new ListTag();
            this.items.forEach(item -> itemsNbt.add(StringTag.valueOf(Registry.ITEM.getKey(item).toString())));
            tag.put(TAG_ITEMS, itemsNbt);
        }

        if (runes != null) {
            tag.putString(TAG_RUNES, runes);
        }

        if (clue != null) {
            tag.putString(TAG_CLUE, clue);
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
        destination.clue = tag.getString(TAG_CLUE);
        destination.difficulty = tag.getFloat(TAG_DIFFICULTY);
        destination.decay = tag.getFloat(TAG_DECAY);

        ListTag itemsList = tag.getList(TAG_ITEMS, 8);
        for (int i = 0; i < itemsList.size(); i++) {
            ResourceLocation res = new ResourceLocation(itemsList.getString(i));
            destination.items.add(Registry.ITEM.get(res));
        }

        return destination;
    }
}
