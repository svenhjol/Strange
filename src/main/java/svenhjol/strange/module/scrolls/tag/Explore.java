package svenhjol.strange.module.scrolls.tag;

import svenhjol.charm.helper.PlayerHelper;
import svenhjol.charm.helper.PosHelper;
import svenhjol.strange.module.scrolls.populator.ExplorePopulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class Explore implements ISerializable {
    public static final String STRUCTURE = "structure";
    public static final String DIMENSION = "dimension";
    public static final String CHEST_START = "chest_start";
    public static final String CHEST_RANGE = "chest_range";
    public static final String CHESTS = "chests";
    public static final String ITEMS = "items";
    public static final String QUEST = "quest";

    private Quest quest;
    private BlockPos structure;
    private List<BlockPos> chests;
    private ResourceLocation dimension;
    private int chestStart;
    private int chestRange;
    private List<ItemStack> items = new ArrayList<>();
    private final Map<Item, Boolean> satisfied = new HashMap<>(); // this is dynamically generated, not stored in nbt

    public Explore(Quest quest) {
        this.quest = quest;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag outTag = new CompoundTag();

        if (!items.isEmpty()) {
            ListTag itemDataTag = new ListTag();
            for (ItemStack stack : items) {
                CompoundTag itemTag = new CompoundTag();
                stack.save(itemTag);
                itemDataTag.add(itemTag);
            }

            outTag.put(ITEMS, itemDataTag);
        }


        if (structure != null)
            outTag.putLong(STRUCTURE, structure.asLong());

        if (chests != null && !chests.isEmpty()) {
            List<Long> chestPositions = chests.stream()
                .map(BlockPos::immutable)
                .map(BlockPos::asLong)
                .collect(Collectors.toList());

            outTag.putLongArray(CHESTS, chestPositions);
        }

        if (dimension != null)
            outTag.putString(DIMENSION, dimension.toString());

        outTag.putInt(CHEST_START, chestStart);
        outTag.putInt(CHEST_RANGE, chestRange);

        return outTag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        chestRange = tag.getInt(CHEST_RANGE);
        chestStart = tag.getInt(CHEST_START);
        structure = tag.contains(STRUCTURE) ? BlockPos.of(tag.getLong(STRUCTURE)) : null;
        dimension = ResourceLocation.tryParse(tag.getString(DIMENSION));

        if (dimension == null)
            dimension = new ResourceLocation("minecraft", "overworld"); // probably not good

        if (tag.contains(CHESTS)) {
            chests = new ArrayList<>();
            long[] chestPositions = tag.getLongArray(CHESTS);
            for (long pos : chestPositions) {
                chests.add(BlockPos.of(pos));
            }
        }

        items = new ArrayList<>();

        ListTag itemDataTag = (ListTag)tag.get(ITEMS);
        if (itemDataTag != null && itemDataTag.size() > 0) {
            for (Tag itemTag : itemDataTag) {
                ItemStack stack = ItemStack.of((CompoundTag)itemTag);
                items.add(stack);
            }
        }
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public BlockPos getStructure() {
        return structure;
    }

    public int getChestStart() {
        return chestStart;
    }

    public int getChestRange() {
        return chestRange;
    }

    public Map<Item, Boolean> getSatisfied() {
        return satisfied;
    }

    public void setStructure(BlockPos structure) {
        this.structure = structure;
    }

    public void setDimension(ResourceLocation dimension) {
        this.dimension = dimension;
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
    }

    public void setChestStart(int chestStart) {
        this.chestStart = chestStart;
    }

    public void setChestRange(int chestRange) {
        this.chestRange = chestRange;
    }

    public boolean isSatisfied() {
        if (items.isEmpty())
            return true;

        return satisfied.size() == items.size() && getSatisfied().values().stream().allMatch(r -> r);
    }

    public void playerTick(Player player) {
        if (player.level.isClientSide || structure == null || (chests != null && !chests.isEmpty()))
            return;

        double dist = PosHelper.getDistanceSquared(player.blockPosition(), structure);
        if (dist < 1200) {
            List<BlockPos> chestPositions = ExplorePopulator.addScrollItemsToChests(player, this);

            if (chestPositions.isEmpty()) {
                quest.abandon(player);
                return;
            }

            quest.setDirty(true);
            chests = chestPositions;
        }
    }

    public void complete(Player player, AbstractVillager merchant) {
        if (items.isEmpty())
            return;

        items.forEach(stack -> {
            for (ItemStack invStack : PlayerHelper.getInventory(player).items) {
                if (ItemStack.matches(stack, invStack)
                    && stack.getOrCreateTag().getString(QUEST).equals(quest.getId())
                ) {
                    invStack.shrink(1);
                }
            }
        });
    }

    public void update(Player player) {
        satisfied.clear();

        items.forEach(itemStack -> {
            ItemStack stack = itemStack.copy();
            Item item = stack.getItem();
            satisfied.put(item, false);

            PlayerHelper.getInventory(player).items.forEach(invStack -> {
                // skip if already added
                if (satisfied.containsKey(item) && satisfied.get(item))
                    return;

                // compare stacks and add the item if found
                if (!stack.isEmpty()
                    && stack.sameItem(invStack)
                    && invStack.getOrCreateTag().getString(QUEST).equals(quest.getId())
                ) {
                    satisfied.put(item, true);
                }
            });
        });
    }
}
