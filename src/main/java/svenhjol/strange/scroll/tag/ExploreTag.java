package svenhjol.strange.scroll.tag;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExploreTag implements ITag {
    public static final String STRUCTURE = "structure";
    public static final String DIMENSION = "dimension";
    public static final String CHEST = "chest";
    public static final String ITEM_DATA = "item_data";
    public static final String QUEST = "quest";

    private QuestTag questTag;
    private BlockPos structurePos;
    private BlockPos chestPos;
    private Identifier dimension;
    private List<ItemStack> items = new ArrayList<>();
    private Map<ItemStack, Boolean> satisfied = new HashMap<>(); // this is dynamically generated, not stored in nbt

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

    public List<ItemStack> getItems() {
        return items;
    }

    public Map<ItemStack, Boolean> getSatisfied() {
        return satisfied;
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

    public boolean isSatisfied() {
        if (items.isEmpty())
            return true;

        return getSatisfied().values().stream().allMatch(r -> r);
    }

    public void inventoryTick(PlayerEntity player) {

    }

    public void complete(PlayerEntity player, MerchantEntity merchant) {
        if (items.isEmpty())
            return;

        items.forEach(stack -> {
            for (ItemStack invStack : player.inventory.main) {
                if (stack.isItemEqualIgnoreDamage(invStack)
                    && stack.getOrCreateTag().getString(QUEST).equals(questTag.getId())
                ) {
                    invStack.decrement(1);
                }
            }
        });
    }

    public void update(PlayerEntity player) {
        satisfied.clear();

        items.forEach(stack -> {
            satisfied.put(stack, false);

            player.inventory.main.forEach(invStack -> {
                if (!stack.isEmpty()
                    && stack.isItemEqualIgnoreDamage(invStack)
                    && invStack.getOrCreateTag().getString(QUEST).equals(questTag.getId())
                ) {
                    satisfied.put(stack, true);
                }
            });
        });
    }
}
