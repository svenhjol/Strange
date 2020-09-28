package svenhjol.strange.scroll.tag;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
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
import net.minecraft.world.World;
import svenhjol.charm.module.VariantChests;
import svenhjol.meson.Meson;
import svenhjol.meson.enums.IVariantMaterial;
import svenhjol.meson.helper.DecorationHelper;
import svenhjol.meson.helper.PosHelper;

import java.util.*;
import java.util.stream.Collectors;

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
        if (player.world.isClient)
            return;

        if (structurePos == null || chestPos != null)
            return;

        double dist = PosHelper.getDistanceSquared(player.getBlockPos(), structurePos);
        if (dist < 1200) {
            chestPos = placeChest(player.world, structurePos, player.getRandom());
            questTag.markDirty(true);
            player.world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 0.7F, 1.15F);
            Meson.LOG.info("Placed chest at: " + chestPos);
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

    private BlockPos placeChest(World world, BlockPos pos, Random random) {
        int checkRange = 32;
        int fallbackRange = 8;
        int startHeight = 32;

        BlockPos pos1 = pos.add(-checkRange, startHeight - (checkRange/2), -checkRange);
        BlockPos pos2 = pos.add(checkRange, startHeight + (checkRange/2), checkRange);

        List<BlockPos> chests = BlockPos.stream(pos1, pos2).map(BlockPos::toImmutable).filter(p -> {
            BlockState state = world.getBlockState(p);
            return state.getBlock() instanceof ChestBlock;
        }).collect(Collectors.toList());

        if (chests.isEmpty()) {
            BlockState chest;
            boolean useVariantChests = Meson.enabled("charm:variant_chests");
            if (useVariantChests) {
                IVariantMaterial material = DecorationHelper.getRandomVariantMaterial(random);
                chest = VariantChests.NORMAL_CHEST_BLOCKS.get(material).getDefaultState();
            } else {
                chest = Blocks.CHEST.getDefaultState();
            }

            int x = pos.getX() - (fallbackRange/2) + random.nextInt(fallbackRange);
            int z = pos.getZ() - (fallbackRange/2) + random.nextInt(fallbackRange);
            BlockPos place = new BlockPos(x, startHeight, z);
            if (!world.getBlockState(place.down()).isOpaque())
                world.setBlockState(place.down(), Blocks.STONE.getDefaultState(), 2);

            world.setBlockState(place, chest, 2);
            chests.add(place);
        }

        BlockPos place = chests.get(random.nextInt(chests.size()));
        BlockEntity blockEntity = world.getBlockEntity(place);

        if (blockEntity instanceof ChestBlockEntity) {
            ChestBlockEntity chestBlockEntity = (ChestBlockEntity)blockEntity;

            // write the items into the loot
            int slot = 0;
            for (ItemStack stack : items) {
                slot += random.nextInt(chestBlockEntity.size() / items.size());
                chestBlockEntity.setStack(slot, stack);
            }
        }

        return place;
    }
}
