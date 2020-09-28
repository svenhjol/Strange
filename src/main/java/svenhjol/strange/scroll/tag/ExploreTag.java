package svenhjol.strange.scroll.tag;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTables;
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
        if (dist < 1000) {
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
        int minRange = 8;
        int maxRange = 16;
        int maxTries = 8; // tries within range
        int start = 32;
        List<BlockPos> placements = new ArrayList<>();

        // assemble list of valid placements
        for (int y = start; y < start + 8; y++) {
            for (int r = minRange; r <= maxRange; r++) {
                for (int tries = 0; tries < maxTries; tries++) {
                    BlockPos tryPlace = new BlockPos(pos.getX() - (r/2) + random.nextInt(r), y, pos.getZ() - (r/2) + random.nextInt(r));
                    BlockState floor = world.getBlockState(tryPlace.down());
                    BlockState above = world.getBlockState(tryPlace.up());
                    if (floor.isOpaque()
                        && (world.isAir(tryPlace) || world.getBlockState(tryPlace).getMaterial() == Material.WATER)
                        && (above.isAir() || above.getMaterial() == Material.WATER)
                    ) {
                        placements.add(tryPlace);
                    }
                }
            }
        }

        // get the placement position for the chest
        BlockPos placePos;
        if (placements.isEmpty()) {
            int x = pos.getX() - (maxRange/2) + random.nextInt(maxRange);
            int y = start;
            int z = pos.getZ() - (maxRange/2) + random.nextInt(maxRange);
            placePos = new BlockPos(x, y, z);
            if (!world.getBlockState(placePos.down()).isOpaque()) {
                world.setBlockState(placePos.down(), Blocks.STONE.getDefaultState(), 2);
            }
        } else {
            placePos = placements.get(random.nextInt(placements.size()));
        }

        BlockState chest;

        // TODO: this is common to stone circle generator
        boolean useVariantChests = Meson.enabled("charm:variant_chests");
        if (useVariantChests) {
            IVariantMaterial material = DecorationHelper.getRandomVariantMaterial(random);
            chest = VariantChests.NORMAL_CHEST_BLOCKS.get(material).getDefaultState();
        } else {
            chest = Blocks.CHEST.getDefaultState();
        }

        // check waterlogged chest
        if (world.getBlockState(placePos).getMaterial() == Material.WATER)
            chest = chest.with(ChestBlock.WATERLOGGED, true);

        // place the chest
        world.setBlockState(placePos, chest, 2);
        BlockEntity blockEntity = world.getBlockEntity(placePos);
        if (blockEntity instanceof ChestBlockEntity) {
            // create normal loot table
            ChestBlockEntity chestBlockEntity = (ChestBlockEntity)blockEntity;
            chestBlockEntity.setLootTable(LootTables.SIMPLE_DUNGEON_CHEST, random.nextLong());

            // write the items into the loot
            int slot = 0;
            for (ItemStack stack : items) {
                slot += random.nextInt(chestBlockEntity.size() / items.size());
                chestBlockEntity.setStack(slot, stack);
            }
        }

        return placePos;
    }
}
