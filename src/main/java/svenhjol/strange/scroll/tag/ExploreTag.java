package svenhjol.strange.scroll.tag;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ExploreTag implements ITag {
    public static final String STRUCTURE = "structure";
    public static final String DIMENSION = "dimension";
    public static final String CHEST = "chest";
    public static final String ITEM_DATA = "item_data";

    private QuestTag questTag;
    private BlockPos structurePos;
    private BlockPos chestPos;
    private Identifier dimension;
    private List<ItemStack> items = new ArrayList<>();

    public ExploreTag(QuestTag questTag) {
        this.questTag = questTag;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag outTag = new CompoundTag();

        if (!items.isEmpty()) {
            ListTag itemDataTag = new ListTag();
            for (ItemStack stack : items) {
                CompoundTag itemTag = new CompoundTag();
                stack.toTag(itemTag);
                itemDataTag.add(itemTag);
            }

            outTag.put(ITEM_DATA, itemDataTag);
        }


        if (structurePos != null)
            outTag.putLong(STRUCTURE, structurePos.asLong());
        if (chestPos != null)
            outTag.putLong(CHEST, chestPos.asLong());

        outTag.putString(DIMENSION, dimension.toString());
        return outTag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        structurePos = tag.contains(STRUCTURE) ? BlockPos.fromLong(tag.getLong(STRUCTURE)) : null;
        chestPos = tag.contains(CHEST) ? BlockPos.fromLong(tag.getLong(CHEST)) : null;
        dimension = Identifier.tryParse(tag.getString(DIMENSION));

        if (dimension == null)
            dimension = new Identifier("minecraft", "overworld"); // probably not good

        items = new ArrayList<>();

        ListTag itemDataTag = (ListTag)tag.get(ITEM_DATA);
        if (itemDataTag != null && itemDataTag.size() > 0) {
            for (Tag itemTag : itemDataTag) {
                ItemStack stack = ItemStack.fromTag((CompoundTag)itemTag);
                items.add(stack);
            }
        }
    }

    public void inventoryTick(PlayerEntity player) {

    }

    public void setStructurePos(BlockPos structurePos) {
        this.structurePos = structurePos;
    }

    public void setDimension(Identifier dimension) {
        this.dimension = dimension;
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
    }
}
