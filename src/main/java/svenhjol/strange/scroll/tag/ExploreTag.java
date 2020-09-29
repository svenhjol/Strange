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
        if (player.world.isClient)
            return;

        if (structure == null || (chests != null && !chests.isEmpty()))
            return;

        double dist = PosHelper.getDistanceSquared(player.getBlockPos(), structure);
        if (dist < 1200) {
            List<BlockPos> chestPositions = addLootToChests(player.world, structure, player.getRandom());
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

    private List<BlockPos> addLootToChests(World world, BlockPos pos, Random random) {
        int checkRange = 32;
        int fallbackRange = 8;
        int startHeight = 32;

        BlockPos pos1 = pos.add(-checkRange, startHeight - (checkRange/2), -checkRange);
        BlockPos pos2 = pos.add(checkRange, startHeight + (checkRange/2), checkRange);

        List<BlockPos> chests = BlockPos.stream(pos1, pos2).map(BlockPos::toImmutable).filter(p -> {
            BlockState state = world.getBlockState(p);
            return state.getBlock() instanceof ChestBlock;
        }).collect(Collectors.toList());

        // if not possible to find any chests, place one in the world.
        if (chests.isEmpty()) {
            BlockState chest;

            // TODO: make this some kind of helper method
            boolean useVariantChests = Meson.enabled("charm:variant_chests");
            if (useVariantChests) {
                IVariantMaterial material = DecorationHelper.getRandomVariantMaterial(random);
                chest = VariantChests.NORMAL_CHEST_BLOCKS.get(material).getDefaultState();
            } else {
                chest = Blocks.CHEST.getDefaultState();
            }

            // get a location near the start position
            BlockPos place = null;

            outerloop:
            for (int sy = startHeight; sy < startHeight + 4; sy++) {
                for (int sx = -fallbackRange; sx <= fallbackRange; sx++) {
                    for (int sz = -fallbackRange; sz <= fallbackRange; sz++) {
                        BlockPos checkPos = new BlockPos(pos.add(sx, sy, sz));
                        if (world.getBlockState(checkPos).isAir()) {
                            place = checkPos;
                            break outerloop;
                        }
                    }
                }
            }

            if (place == null) {
                int x = -(fallbackRange / 2) + random.nextInt(fallbackRange / 2);
                int z = -(fallbackRange / 2) + random.nextInt(fallbackRange / 2);
                place = new BlockPos(pos.add(x, startHeight, z));
            }

            // build a little 3x3x3 cube of air to house the chest
            for (int py = -1; py <= 1; py++) {
                BlockState state = py == -1 ? Blocks.STONE.getDefaultState() : Blocks.AIR.getDefaultState();
                for (int px = -1; px <= 1; px++) {
                    for (int pz = -1; pz <= 1; pz++) {
                        world.setBlockState(place.add(px, py, pz), state, 2);
                    }
                }
            }
            world.setBlockState(place, chest, 2);
            chests.add(place);
        }

        List<BlockPos> placements = new ArrayList<>();

        // select a chest at random and write the loot into it
        int slot = 0;
        for (ItemStack stack : items) {
            BlockPos place = chests.get(random.nextInt(chests.size()));
            BlockEntity blockEntity = world.getBlockEntity(place);

            if (blockEntity instanceof ChestBlockEntity) {
                ChestBlockEntity chestBlockEntity = (ChestBlockEntity) blockEntity;
                chestBlockEntity.setStack(slot++, stack);
            }
            placements.add(place);
        }

        return placements;
    }
}
