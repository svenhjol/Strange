package svenhjol.strange.scrolls.populator;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapIcon;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.StructureFeature;
import svenhjol.charm.base.enums.IVariantMaterial;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.helper.*;
import svenhjol.charm.module.VariantChests;
import svenhjol.strange.scrolls.JsonDefinition;
import svenhjol.strange.scrolls.tag.Explore;
import svenhjol.strange.scrolls.tag.Quest;

import java.util.*;
import java.util.stream.Collectors;

public class ExplorePopulator extends Populator {
    public static final String ITEMS = "items";
    public static final String SETTINGS = "settings";
    public static final String STRUCTURE = "structure";

    public ExplorePopulator(ServerPlayerEntity player, Quest quest, JsonDefinition definition) {
        super(player, quest, definition);
    }

    @Override
    public void populate() {
        Map<String, Map<String, Map<String, String>>> explore = definition.getExplore();
        List<ItemStack> items = new ArrayList<>();

        if (explore.isEmpty())
            return;


        // populate the items for the quest
        if (explore.containsKey(ITEMS))
            items = parseItems(explore.get(ITEMS), false);

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

        // get a random distance based on min and max
        BlockPos structurePos = PosHelper.addRandomOffset(pos, world.random, minDistance, maxDistance);

        // resolve the structure type from the registry, falling back to mineshaft
        StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(new Identifier(type));
        if (structureFeature == null) {
            structureFeature = Registry.STRUCTURE_FEATURE.get(new Identifier("minecraft:mineshaft"));
            if (structureFeature == null)
                fail("Could not find specified structure type or mineshaft");
        }


        // locate structure in the world
        BlockPos foundPos = world.locateStructure(structureFeature, structurePos, 500, true);

        if (foundPos == null)
            fail("Could not locate structure");


        // set the quest details like items and structure to find
        quest.getExplore().setItems(items);
        quest.getExplore().setDimension(DimensionHelper.getDimension(world));
        quest.getExplore().setStructure(foundPos);
        quest.getExplore().setChestRange(chestRange);
        quest.getExplore().setChestStart(chestStart);

        // give map to the location
        ItemStack map = MapHelper.getMap(world, foundPos, new TranslatableText(quest.getTitle()), MapIcon.Type.TARGET_X, 0x007700);
        PlayerHelper.addOrDropStack(player, map);
    }

    public static List<BlockPos> addLootToChests(PlayerEntity player, Explore explore) {
        World world = player.world;
        BlockPos pos = explore.getStructure();
        List<ItemStack> items = explore.getItems();
        Random random = player.getRandom();

        // normalize, prevent stupid spawning in the sky
        if (pos.getY() != 0)
            pos = new BlockPos(pos.getX(), 0, pos.getZ());

        int fallbackRange = 8;
        int chestRange = explore.getChestRange();
        int chestStart = explore.getChestStart();

        BlockPos pos1 = pos.add(-chestRange, chestStart - (chestRange / 4), -chestRange);
        BlockPos pos2 = pos.add(chestRange, chestStart + (chestRange / 4), chestRange);

        List<BlockPos> chests = BlockPos.stream(pos1, pos2).map(BlockPos::toImmutable).filter(p -> {
            BlockState state = world.getBlockState(p);
            return state.getBlock() instanceof ChestBlock;
        }).collect(Collectors.toList());

        // if not possible to find any chests, place one in the world.
        if (chests.isEmpty()) {
            BlockState chest;

            // TODO: make this some kind of helper method
            boolean useVariantChests = ModuleHandler.enabled("charm:variant_chests");
            if (useVariantChests) {
                IVariantMaterial material = DecorationHelper.getRandomVariantMaterial(random);
                chest = VariantChests.NORMAL_CHEST_BLOCKS.get(material).getDefaultState();
            } else {
                chest = Blocks.CHEST.getDefaultState();
            }

            // get a location near the start position
            BlockPos place = null;

            outerloop:
            for (int sy = chestStart; sy < chestStart + 4; sy++) {
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
                place = new BlockPos(pos.add(x, chestStart, z));
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
        for (ItemStack itemStack : items) {
            ItemStack stack = itemStack.copy();
            BlockPos place = chests.get(random.nextInt(chests.size()));
            BlockEntity blockEntity = world.getBlockEntity(place);

            if (blockEntity instanceof ChestBlockEntity) {
                ChestBlockEntity chestBlockEntity = (ChestBlockEntity) blockEntity;
                chestBlockEntity.setStack(slot++, stack);

                // for easier identification of target chest, set the block under the chest
                world.setBlockState(place.down(), Blocks.POLISHED_DIORITE.getDefaultState(), 2);
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
