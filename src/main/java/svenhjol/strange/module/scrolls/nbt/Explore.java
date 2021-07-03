package svenhjol.strange.module.scrolls.nbt;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.helper.PlayerHelper;
import svenhjol.charm.helper.PosHelper;
import svenhjol.strange.module.scrolls.ScrollHelper;
import svenhjol.strange.module.scrolls.populator.ExplorePopulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Explore implements IQuestSerializable {
    public static final String STRUCTURE_NBT = "structure";
    public static final String DIMENSION_NBT = "dimension";
    public static final String CHEST_START_NBT = "chest_start";
    public static final String CHEST_RANGE_NBT = "chest_range";
    public static final String CHESTS_NBT = "chests";
    public static final String ITEMS_NBT = "items";
    public static final String QUEST_NBT = "quest";

    private final Quest quest;
    private BlockPos structure;
    private List<BlockPos> chests;
    private ResourceLocation dimension;
    private int chestStart;
    private int chestRange;
    private List<ItemStack> items = new ArrayList<>();

    // dynamically generated, not stored in nbt
    private final Map<Item, Boolean> satisfied = new HashMap<>();

    public Explore(Quest quest) {
        this.quest = quest;
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag outTag = new CompoundTag();

        if (!items.isEmpty()) {
            ListTag itemDataTag = new ListTag();
            for (ItemStack stack : items) {
                CompoundTag itemTag = new CompoundTag();
                stack.save(itemTag);
                itemDataTag.add(itemTag);
            }

            outTag.put(ITEMS_NBT, itemDataTag);
        }

        if (structure != null)
            outTag.putLong(STRUCTURE_NBT, structure.asLong());

        if (chests != null && !chests.isEmpty()) {
            List<Long> chestPositions = chests.stream()
                .map(BlockPos::immutable)
                .map(BlockPos::asLong)
                .collect(Collectors.toList());

            outTag.putLongArray(CHESTS_NBT, chestPositions);
        }

        if (dimension != null)
            outTag.putString(DIMENSION_NBT, dimension.toString());

        outTag.putInt(CHEST_START_NBT, chestStart);
        outTag.putInt(CHEST_RANGE_NBT, chestRange);

        return outTag;
    }

    @Override
    public void fromNbt(CompoundTag nbt) {
        chestRange = nbt.getInt(CHEST_RANGE_NBT);
        chestStart = nbt.getInt(CHEST_START_NBT);
        structure = nbt.contains(STRUCTURE_NBT) ? BlockPos.of(nbt.getLong(STRUCTURE_NBT)) : null;
        dimension = ResourceLocation.tryParse(nbt.getString(DIMENSION_NBT));

        if (dimension == null)
            dimension = ScrollHelper.FALLBACK_DIMENSION;

        if (nbt.contains(CHESTS_NBT)) {
            chests = new ArrayList<>();
            long[] chestPositions = nbt.getLongArray(CHESTS_NBT);
            for (long pos : chestPositions) {
                chests.add(BlockPos.of(pos));
            }
        }

        items = new ArrayList<>();

        ListTag itemDataTag = (ListTag) nbt.get(ITEMS_NBT);
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
        if (dist < ExplorePopulator.POPULATE_DISTANCE) {
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
                    && stack.getOrCreateTag().getString(QUEST_NBT).equals(quest.getId())
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
                    && invStack.getOrCreateTag().getString(QUEST_NBT).equals(quest.getId())
                ) {
                    satisfied.put(item, true);
                }
            });
        });
    }
}
