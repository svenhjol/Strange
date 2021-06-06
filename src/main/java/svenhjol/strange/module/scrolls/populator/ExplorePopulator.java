package svenhjol.strange.module.scrolls.populator;

import svenhjol.charm.Charm;
import svenhjol.charm.enums.IVariantMaterial;
import svenhjol.charm.handler.ModuleHandler;
import svenhjol.charm.helper.*;
import svenhjol.charm.module.core.Core;
import svenhjol.charm.module.variant_chests.VariantChests;
import svenhjol.strange.module.scrolls.Scrolls;
import svenhjol.strange.module.scrolls.tag.Explore;
import svenhjol.strange.module.scrolls.tag.Quest;

import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

public class ExplorePopulator extends BasePopulator {
    public static final String ITEMS = "items";
    public static final String SETTINGS = "settings";
    public static final String STRUCTURE = "structure";

    public static final int MAP_COLOR = 0x007700;

    private static final BlockState OVERWORLD_PLATFORM_BASE = Blocks.STONE.defaultBlockState();
    private static final BlockState NETHER_PLATFORM_BASE = Blocks.BLACKSTONE.defaultBlockState();
    private static final BlockState END_PLATFORM_BASE = Blocks.END_STONE_BRICKS.defaultBlockState();

    public ExplorePopulator(ServerPlayer player, Quest quest) {
        super(player, quest);
    }

    @Override
    public void populate() {
        Map<String, Map<String, Map<String, String>>> explore = definition.getExplore();
        List<ItemStack> items = new ArrayList<>();

        if (explore.isEmpty())
            return;


        // populate the items for the quest
        if (explore.containsKey(ITEMS))
            items = parseItems(explore.get(ITEMS), 4, false);

        if (items.isEmpty())
            fail("No items found for quest");


        // set the quest tag of each item stack to the quest ID so we can match it later
        items.forEach(item -> item.getOrCreateTag().putString(Explore.QUEST, quest.getId()));


        // parse settings
        Map<String, Map<String, String>> exploreMap = explore.getOrDefault(SETTINGS, new HashMap<>());
        Map<String, String> structureSettings = exploreMap.getOrDefault(STRUCTURE, new HashMap<>());

        String type = structureSettings.getOrDefault("type", "minecraft:mineshaft");
        int minDistance = Integer.parseInt(structureSettings.getOrDefault("min_distance", "750"));
        int maxDistance = Integer.parseInt(structureSettings.getOrDefault("max_distance", "1500"));
        int chestStart = Integer.parseInt(structureSettings.getOrDefault("chest_start", "32"));
        int chestRange = Integer.parseInt(structureSettings.getOrDefault("chest_range", "24"));
        boolean skipExistingChunks = Boolean.parseBoolean(structureSettings.getOrDefault("skip_existing_chunks", "false"));

        // get a random distance based on min and max
        BlockPos structurePos = PosHelper.addRandomOffset(pos, world.random, minDistance, maxDistance);

        // resolve the structure type from the registry
        StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(new ResourceLocation(type));
        if (structureFeature == null)
            fail("Could not find specified structure type");


        // locate structure in the world
        BlockPos foundPos = world.findNearestMapFeature(structureFeature, structurePos, 500, skipExistingChunks);

        if (foundPos == null)
            fail("Could not locate structure");


        // set the quest details like items and structure to find
        quest.getExplore().setItems(items);
        quest.getExplore().setDimension(DimensionHelper.getDimension(world));
        quest.getExplore().setStructure(foundPos);
        quest.getExplore().setChestRange(chestRange);
        quest.getExplore().setChestStart(chestStart);

        // give map to the location
        PlayerHelper.addOrDropStack(player, getMap());
    }

    @Override
    public ItemStack getMap() {
        BlockPos pos = quest.getExplore().getStructure();
        if (pos == null)
            return ItemStack.EMPTY;

        Charm.LOG.info("[ExplorePopulator] Map created for explore quest at pos: " + pos);
        return MapHelper.getMap(world, pos, new TranslatableComponent(quest.getTitle()), MapDecoration.Type.TARGET_X, MAP_COLOR);
    }

    public static List<BlockPos> addScrollItemsToChests(Player player, Explore explore) {
        Level world = player.level;
        BlockPos pos = explore.getStructure();
        List<ItemStack> items = explore.getItems();
        Random random = player.getRandom();

        // normalize, prevent stupid spawning in the sky
        if (pos.getY() != 0)
            pos = new BlockPos(pos.getX(), 0, pos.getZ());

        int fallbackRange = 8;
        int chestRange = explore.getChestRange();
        int chestStart = explore.getChestStart();

        BlockPos pos1 = pos.offset(-chestRange, chestStart - chestRange, -chestRange);
        BlockPos pos2 = pos.offset(chestRange, chestStart + chestRange, chestRange);

        List<BlockPos> chests = BlockPos.betweenClosedStream(pos1, pos2).map(BlockPos::immutable).filter(p -> {
            BlockState state = world.getBlockState(p);
            return state.getBlock() instanceof ChestBlock;
        }).collect(Collectors.toList());

        // if not possible to find any chests, place one in the world
        if (chests.isEmpty()) {
            BlockState chest;

            // TODO: make this some kind of helper method
            boolean useVariantChests = ModuleHandler.enabled("charm:variant_chests");
            if (useVariantChests) {
                IVariantMaterial material = DecorationHelper.getRandomVariantMaterial(random);
                chest = VariantChests.NORMAL_CHEST_BLOCKS.get(material).defaultBlockState();
            } else {
                chest = Blocks.CHEST.defaultBlockState();
            }

            // get a location near the start position
            BlockPos place = null;

            findloop:
            for (int sy = chestStart; sy < chestStart + 4; sy++) {
                for (int sx = -fallbackRange; sx <= fallbackRange; sx++) {
                    for (int sz = -fallbackRange; sz <= fallbackRange; sz++) {
                        BlockPos checkPos = new BlockPos(pos.offset(sx, sy, sz));
                        if (world.getBlockState(checkPos).isAir()) {
                            place = checkPos;
                            break findloop;
                        }
                    }
                }
            }

            if (place == null) {
                int x = -(fallbackRange / 2) + random.nextInt(fallbackRange / 2);
                int z = -(fallbackRange / 2) + random.nextInt(fallbackRange / 2);
                place = new BlockPos(pos.offset(x, chestStart, z));
            }

            // build a little 3x3x3 cube of air/water to house the chest
            BlockState platformBaseState;
            if (DimensionHelper.isDimension(world, ServerLevel.NETHER)) {
                platformBaseState = NETHER_PLATFORM_BASE;
            } else if (DimensionHelper.isDimension(world, ServerLevel.END)) {
                platformBaseState = END_PLATFORM_BASE;
            } else {
                platformBaseState = OVERWORLD_PLATFORM_BASE;
            }

            for (int py = -1; py <= 1; py++) {
                BlockState state = py == -1 ? platformBaseState : Blocks.AIR.defaultBlockState();
                for (int px = -1; px <= 1; px++) {
                    for (int pz = -1; pz <= 1; pz++) {
                        BlockPos buildPos = place.offset(px, py, pz);
                        if (state.isAir() && world.getBlockState(buildPos).getMaterial() == Material.WATER)
                            state = Blocks.WATER.defaultBlockState();

                        world.setBlock(buildPos, state, 2);
                    }
                }
            }
            world.setBlock(place, chest, 2);
            chests.add(place);
        }

        Collections.shuffle(chests);
        List<BlockPos> placements = new ArrayList<>();

        // try and write the item stack into the available chests
        for (ItemStack itemStack : items) {
            ItemStack stack = itemStack.copy();
            boolean didPlaceItem = false;

            placeloop:
            for (BlockPos place : chests) {
                BlockEntity blockEntity = world.getBlockEntity(place);

                // iterate over all chest slots to find an empty slot for the item
                if (blockEntity instanceof ChestBlockEntity) {
                    ChestBlockEntity chestBlockEntity = (ChestBlockEntity) blockEntity;
                    for (int s = 0; s < chestBlockEntity.getContainerSize(); s++) {
                        ItemStack stackInSlot = chestBlockEntity.getItem(s);
                        if (stackInSlot.isEmpty()) {
                            chestBlockEntity.setItem(s, stack);

                            // if in debug mode then place glowstone beneath the chest for easier identification
                            if (Core.debug) {
                                BlockPos downPos = place.below();
                                BlockEntity downBlockEntity = world.getBlockEntity(downPos);
                                if (downBlockEntity != null)
                                    continue; // don't set the block underneath if it's a blockentity/tile

                                world.setBlock(place.below(), Blocks.GLOWSTONE.defaultBlockState(), 2);
                            }

                            placements.add(place);
                            didPlaceItem = true;
                            break placeloop;
                        }
                    }
                }
            }


            // if unable to place the item in any of the chests, try and create a trapped chest next to existing one.
            if (!didPlaceItem) {
                BlockPos chestPos = chests.get(random.nextInt(chests.size()));
                BlockState chestState = world.getBlockState(chestPos);
                Direction facing = chestState.getValue(ChestBlock.FACING);
                List<BlockPos> tryPositions = new ArrayList<>();

                if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                    tryPositions.addAll(Arrays.asList(chestPos.relative(Direction.EAST), chestPos.relative(Direction.WEST)));
                } else {
                    tryPositions.addAll(Arrays.asList(chestPos.relative(Direction.NORTH), chestPos.relative(Direction.SOUTH)));
                }

                for (BlockPos tryPos : tryPositions) {
                    BlockState state = world.getBlockState(tryPos);
                    if (state.isAir() || state.getMaterial() == Material.WATER) {
                        world.setBlockAndUpdate(pos, Blocks.TRAPPED_CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
                        ChestBlockEntity chestBlockEntity = (ChestBlockEntity)world.getBlockEntity(pos);
                        if (chestBlockEntity != null) {
                            chestBlockEntity.setItem(0, stack);
                            placements.add(tryPos);
                            break;
                        }
                    }
                }
            }
        }

        if (!placements.isEmpty()) {
            if (Scrolls.exploreHint) {
                player.displayClientMessage(new TranslatableComponent("gui.strange.scrolls.explore_placed"), true);
                player.level.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.6F, 1.2F);
            }

            placements.forEach(p -> Charm.LOG.info("[ExplorePopulator] Added quest loot to chest at: " + p.toString()));
        }

        return placements;
    }

    private void fail(String message) {
        throw new IllegalStateException("Could not start exploration quest: " + message);
    }
}
