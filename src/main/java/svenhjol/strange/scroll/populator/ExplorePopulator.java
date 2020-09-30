package svenhjol.strange.scroll.populator;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapIcon;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.StructureFeature;
import svenhjol.charm.module.VariantChests;
import svenhjol.meson.Meson;
import svenhjol.meson.enums.IVariantMaterial;
import svenhjol.meson.helper.DecorationHelper;
import svenhjol.meson.helper.DimensionHelper;
import svenhjol.meson.helper.MapHelper;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.strange.helper.RunestoneHelper;
import svenhjol.strange.module.Ruins;
import svenhjol.strange.scroll.JsonDefinition;
import svenhjol.strange.scroll.tag.ExploreTag;
import svenhjol.strange.scroll.tag.QuestTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ExplorePopulator extends Populator {

    public ExplorePopulator(ServerPlayerEntity player, QuestTag quest, JsonDefinition definition) {
        super(player, quest, definition);
    }

    @Override
    public void populate() {
        List<String> explore = definition.getExplore();

        if (explore.isEmpty())
            return;

        // find the structure to explore
        BlockPos min = RunestoneHelper.addRandomOffset(pos, world.random, 1000);
        BlockPos max = RunestoneHelper.addRandomOffset(min, world.random, 500);

        StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(Ruins.STRUCTURE_ID);
        if (structureFeature == null) {
            structureFeature = Registry.STRUCTURE_FEATURE.get(new Identifier("mineshaft"));
            if (structureFeature == null)
                fail("Could not find ruin or mineshaft");
        }

        BlockPos foundPos = world.locateStructure(structureFeature, max, 500, true);
        if (foundPos == null)
            fail("Could not locate structure");


        // populate the items for the quest
        // TODO: handle fun names for items
        List<ItemStack> items = new ArrayList<>();

        for (String stackName : explore) {
            ItemStack stack = getItemFromKey(stackName);
            if (stack == null)
                continue;

            // set the quest tag of the stack to the quest ID so we can match it later
            stack.getOrCreateTag().putString(ExploreTag.QUEST, quest.getId());
            items.add(stack);
        }

        quest.getExplore().setItems(items);
        quest.getExplore().setDimension(DimensionHelper.getDimension(world));
        quest.getExplore().setStructure(foundPos);


        // give map to the location
        ItemStack map = MapHelper.getMap(world, foundPos, new TranslatableText(quest.getTitle()), MapIcon.Type.TARGET_X, 0x007700);
        PlayerHelper.addOrDropStack(player, map);
    }

    public static List<BlockPos> addLootToChests(World world, BlockPos pos, List<ItemStack> items, Random random) {
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

    private void fail(String message) {
        // TODO: handle this fail condition
        throw new RuntimeException("Could not start exploration quest: " + message);
    }
}
