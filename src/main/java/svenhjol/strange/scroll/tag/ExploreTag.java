package svenhjol.strange.scroll.tag;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import svenhjol.meson.Meson;
import svenhjol.meson.helper.PosHelper;
import svenhjol.strange.scroll.populator.ExplorePopulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExploreTag implements ITag {
    public static final String STRUCTURE = "structure";
    public static final String DIMENSION = "dimension";
    public static final String CHESTS = "chests";
    public static final String ITEMS = "items";
    public static final String QUEST = "quest";

    private QuestTag questTag;
    private BlockPos structure;
    private List<BlockPos> chests;
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

            outTag.put(ITEMS, itemDataTag);
        }


        if (structure != null)
            outTag.putLong(STRUCTURE, structure.asLong());

        if (chests != null && !chests.isEmpty()) {
            List<Long> chestPositions = chests.stream()
                .map(BlockPos::toImmutable)
                .map(BlockPos::asLong)
                .collect(Collectors.toList());

            outTag.putLongArray(CHESTS, chestPositions);
        }

        if (dimension != null)
            outTag.putString(DIMENSION, dimension.toString());

        return outTag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        structure = tag.contains(STRUCTURE) ? BlockPos.fromLong(tag.getLong(STRUCTURE)) : null;
        dimension = Identifier.tryParse(tag.getString(DIMENSION));

        if (dimension == null)
            dimension = new Identifier("minecraft", "overworld"); // probably not good

        if (tag.contains(CHESTS)) {
            chests = new ArrayList<>();
            long[] chestPositions = tag.getLongArray(CHESTS);
            for (long pos : chestPositions) {
                chests.add(BlockPos.fromLong(pos));
            }
        }

        items = new ArrayList<>();

        ListTag itemDataTag = (ListTag)tag.get(ITEMS);
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

    public void setStructure(BlockPos structure) {
        this.structure = structure;
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
        if (player.world.isClient || structure == null || (chests != null && !chests.isEmpty()))
            return;

        double dist = PosHelper.getDistanceSquared(player.getBlockPos(), structure);
        if (dist < 1200) {
            List<BlockPos> chestPositions = ExplorePopulator.addLootToChests(player, this);
            questTag.markDirty(true);

            player.world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 0.55F, 1.2F);
            chestPositions.forEach(pos -> Meson.LOG.debug("Added to chest at: " + pos.toShortString()));

            chests = chestPositions;
        }
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
