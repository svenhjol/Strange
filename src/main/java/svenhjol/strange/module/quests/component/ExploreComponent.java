package svenhjol.strange.module.quests.component;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.helper.*;
import svenhjol.strange.Strange;
import svenhjol.strange.module.quests.IQuestComponent;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.Quests;
import svenhjol.strange.module.quests.helper.QuestDefinitionHelper;

import java.util.*;

public class ExploreComponent implements IQuestComponent {
    public static final String STRUCTURE_TAG = "structure";
    public static final String DIMENSION_TAG = "dimension";
    public static final String SETTINGS_TAG = "settings";
    public static final String TYPE_TAG = "type";
    public static final String MIN_DISTANCE_TAG = "min_distance";
    public static final String MAX_DISTANCE_TAG = "max_distance";
    public static final String CHEST_START_TAG = "chest_start";
    public static final String CHEST_RANGE_TAG = "chest_range";
    public static final String SKIP_EXISTING_CHUNKS_TAG = "skip_existing_chunks";
    public static final String CHESTS_TAG = "chests";
    public static final String STRUCTURE_POS_TAG = "structure_pos";
    public static final String ITEMS_TAG = "items";
    public static final String QUEST_TAG = "strange_quest"; // Required items are tagged with the quest ID.

    public static final String DEFAULT_MIN_DISTANCE = "750";
    public static final String DEFAULT_MAX_DISTANCE = "1500";
    public static final String DEFAULT_CHEST_START = "32";
    public static final String DEFAULT_CHEST_RANGE = "24";
    public static final String DEFAULT_SKIP_EXISTING_CHUNKS = "false";
    public static final int MAX_ITEMS = 4;
    public static final int FALLBACK_RANGE = 8;
    public static final int POPULATE_DISTANCE = 1200;
    public static final int MAP_COLOR = 0x007700;
    public static final MapDecoration.Type MAP_TAG = MapDecoration.Type.TARGET_X;

    private final Quest quest;
    private ResourceLocation dimension;
    private BlockPos structurePos;
    private int chestStart;
    private int chestRange;

    private final List<BlockPos> chests = new ArrayList<>();
    private final List<ItemStack> items = new ArrayList<>();
    private final Map<Item, Boolean> satisfied = new HashMap<>();

    public ExploreComponent(Quest quest) {
        this.quest = quest;
    }

    @Override
    public String getId() {
        return "explore";
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean isSatisfied(Player player) {
        if (isEmpty()) return true; // To bypass quests that don't have an explore component.
        return satisfied.size() == items.size() && satisfied.values().stream().allMatch(b -> b);
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public Map<Item, Boolean> getSatisfied() {
        return satisfied;
    }

    @Override
    public void complete(Player player, @Nullable AbstractVillager merchant) {
        if (isEmpty()) return;

        items.forEach(stack -> {
            for (ItemStack inv : player.getInventory().items) {
                if (ItemStack.matches(stack, inv) && NbtHelper.getString(inv, QUEST_TAG, "").equals(quest.getId())) {
                    inv.shrink(1);
                }
            }
        });
    }

    @Override
    public void update(Player player) {
        satisfied.clear();

        items.forEach(stack -> {
            var theStack = stack.copy();
            var item = stack.getItem();
            satisfied.put(item, false);

            for (ItemStack inv : player.getInventory().items) {
                // If the item has already been found, return early to save pointless loops.
                if (satisfied.getOrDefault(item, false)) return;

                if (inv.isEmpty() || !inv.hasTag() || !inv.sameItem(theStack)) continue;

                // Compare the item's quest tag against the quest ID.
                if (NbtHelper.getString(inv, QUEST_TAG, "").equals(quest.getId())) {
                    satisfied.put(item, true);
                }
            }
        });
    }

    @Override
    public boolean start(Player player) {
        if (player.level.isClientSide) return false;

        items.clear();

        var serverPlayer = (ServerPlayer) player;
        var pos = serverPlayer.blockPosition();
        var random = serverPlayer.getRandom();
        var level = (ServerLevel) serverPlayer.getLevel();

        var definition = quest.getDefinition();
        var exploreDefinition = definition.getExplore();
        if (exploreDefinition == null || exploreDefinition.isEmpty()) {
            // No definition - no explore quest. Exit cleanly.
            return true;
        }

        // The explore definition has two sections:
        //   1. items
        //   2. settings
        //
        // The items section consists of one or more special items that must be found in a chest within the structure.
        // The settings section describes the type of structure and where to spawn the chest.

        var itemMap = exploreDefinition.getOrDefault(ITEMS_TAG, null);
        if (itemMap == null || itemMap.isEmpty()) {
            // No items - no explore quest. Exit cleanly.
            return true;
        }

        var parsedItems = QuestDefinitionHelper.parseItems((ServerPlayer) player, itemMap, MAX_ITEMS);
        if (parsedItems.isEmpty()) {
            LogHelper.error(Strange.MOD_ID, getClass(), "Missing items tag, cannot start quest.");
            return false; // Missing items tag.
        }

        var settingsMap = exploreDefinition.getOrDefault(SETTINGS_TAG, null);
        if (settingsMap == null || settingsMap.isEmpty()) {
            LogHelper.error(Strange.MOD_ID, getClass(), "Missing settings tag, cannot start quest.");
            return false; // Missing settings tag.
        }

        var structureMap = settingsMap.getOrDefault(STRUCTURE_TAG, null);
        if (structureMap == null || structureMap.isEmpty()) {
            LogHelper.error(Strange.MOD_ID, getClass(), "Missing structure tag, cannot start quest.");
            return false; // Missing structure tag.
        }

        var structureId = structureMap.getOrDefault(TYPE_TAG, null);
        if (structureId == null) {
            LogHelper.error(Strange.MOD_ID, getClass(), "Missing structure type, cannot start quest.");
            return false; // Missing structure type.
        }

        var structureFeature = Registry.STRUCTURE_FEATURE.get(new ResourceLocation(structureId));
        if (structureFeature == null) {
            LogHelper.error(Strange.MOD_ID, getClass(), "Invalid structure type, cannot start quest.");
            return false; // Invalid structure type.
        }

        var minDistance = Integer.parseInt(structureMap.getOrDefault(MIN_DISTANCE_TAG, DEFAULT_MIN_DISTANCE));
        var maxDistance = Integer.parseInt(structureMap.getOrDefault(MAX_DISTANCE_TAG, DEFAULT_MAX_DISTANCE));
        var skipExistingChunks = Boolean.parseBoolean(structureMap.getOrDefault(SKIP_EXISTING_CHUNKS_TAG, DEFAULT_SKIP_EXISTING_CHUNKS));
        chestStart = Integer.parseInt(structureMap.getOrDefault(CHEST_START_TAG, DEFAULT_CHEST_START));
        chestRange = Integer.parseInt(structureMap.getOrDefault(CHEST_RANGE_TAG, DEFAULT_CHEST_RANGE));

        // Find the structure closest to a blockpos within min and max distance from player.
        var blockPos = WorldHelper.addRandomOffset(pos, random, minDistance, maxDistance);
        structurePos = level.findNearestMapFeature(structureFeature, blockPos, 500, skipExistingChunks);// TODO: what is 500 here

        if (structurePos == null) {
            LogHelper.error(Strange.MOD_ID, getClass(), "Could not find structure, cannot start quest.");
            return false; // Could not find structure.
        }

        // Set the tag of each of these items to the quest's ID so we can track them.
        parsedItems.forEach(item -> {
            item.getOrCreateTag().putString(QUEST_TAG, quest.getId());
            items.add(item);
        });

        // Set to the player's current dimension.
        dimension = DimensionHelper.getDimension(level);

        // Provide a map to the player.
        provideMap(serverPlayer);

        return true;
    }

    @Override
    public void playerTick(Player player) {
        if (player.level.isClientSide) return;
        if (structurePos == null) return; // No position has been calculated for structure to explore.
        if (!DimensionHelper.isDimension(player.level, dimension)) return; // Must match dimension that quest was started in.
        if (!chests.isEmpty()) return; // Means they've already been populated with items for this exploration.

        // At this point we check if the player is within distance of the target structure pos.
        // Generate the chests and let the player know they're in the right vicinity for searching.

        var serverPlayer = (ServerPlayer) player;
        var playerPos = serverPlayer.blockPosition();
        var level = serverPlayer.getLevel();
        var random = serverPlayer.getRandom();
        var dist = WorldHelper.getDistanceSquared(playerPos, structurePos);

        if (dist > POPULATE_DISTANCE) return;

        // Prevent spawning in the sky.
        if (structurePos.getY() != 0) {
            structurePos = new BlockPos(structurePos.getX(), 0, structurePos.getZ());
        }

        // Find all chests within range.
        var pos1 = structurePos.offset(-chestRange, chestStart - chestRange, -chestRange);
        var pos2 = structurePos.offset(chestRange, chestStart + chestRange, chestRange);
        var foundChestPositions = BlockPos.betweenClosedStream(pos1, pos2).map(BlockPos::immutable).filter(p -> {
            BlockState state = level.getBlockState(p);
            return state.getBlock() instanceof ChestBlock;
        }).toList();

        if (foundChestPositions.isEmpty()) {
            // If no chests have been found then place one in the world.
            var chest = Blocks.CHEST.defaultBlockState();
            BlockPos foundPos = null;

            FIND:
            for (int sy = chestStart; sy < chestStart + 4; sy++) {
                for (int sx = -FALLBACK_RANGE; sx <= FALLBACK_RANGE; sx++) {
                    for (int sz = -FALLBACK_RANGE; sz <= FALLBACK_RANGE; sz++) {
                        var checkPos = new BlockPos(structurePos.offset(sx, sy, sz));
                        if (level.getBlockState(checkPos).isAir()) {
                            foundPos = checkPos;
                            break FIND;
                        }
                    }
                }
            }

            if (foundPos == null) {
                var x = -(FALLBACK_RANGE / 2) + random.nextInt(FALLBACK_RANGE / 2);
                var z = -(FALLBACK_RANGE / 2) + random.nextInt(FALLBACK_RANGE / 2);
                foundPos = new BlockPos(structurePos.offset(x, chestStart, z));
            }

            // Generate a platform and airspace for the chest.
            // TODO: move RepositionTarget#makePlatform to WorldHelper
            var block = WorldHelper.getSurfaceBlockForDimension(level);
            for (int py = -1; py <= 1; py++) {
                BlockState state = py == -1 ? block : Blocks.AIR.defaultBlockState();
                for (int px = -1; px <= 1; px++) {
                    for (int pz = -1; pz <= 1; pz++) {
                        var buildPos = foundPos.offset(px, py, pz);
                        if (state.isAir() && level.getBlockState(buildPos).getMaterial() == Material.WATER) {
                            state = Blocks.WATER.defaultBlockState();
                        }
                        level.setBlockAndUpdate(buildPos, state);
                    }
                }
            }

            level.setBlockAndUpdate(foundPos, chest);
            foundChestPositions.add(foundPos);
        }

        // Place quest items in found chest(s).
        Collections.shuffle(foundChestPositions, random);
        chests.clear();

        for (ItemStack stack : items) {
            var theStack = stack.copy();
            var placed = false;

            PLACE:
            for (BlockPos placePos : foundChestPositions) {

                if (level.getBlockEntity(placePos) instanceof ChestBlockEntity chest) {
                    for (int s = 0; s < chest.getContainerSize(); s++) {
                        var stackInSlot = chest.getItem(s);
                        if (stackInSlot.isEmpty()) {
                            chest.setItem(s, theStack);

                            // Put glowstone underneath the chest that contains the item.
                            if (Quests.showExplorePlacement || DebugHelper.isDebugMode()) {
                                var belowPos = placePos.below();
                                var belowBlockEntity = level.getBlockEntity(belowPos);
                                if (belowBlockEntity != null) continue; // Don't trash a block entity underneath, skip.
                                level.setBlockAndUpdate(belowPos, Blocks.GLOWSTONE.defaultBlockState());
                            }

                            chests.add(placePos);
                            placed = true;
                            break PLACE;
                        }
                    }
                }
            }

            if (!placed) {
                // If unable to place then create a trapped chest next to an existing found chest.
                var blockPos = foundChestPositions.get(random.nextInt(foundChestPositions.size()));
                var blockState = level.getBlockState(blockPos);
                var facing = blockState.getValue(ChestBlock.FACING);
                List<BlockPos> potentialPositions = new ArrayList<>();

                if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                    potentialPositions.addAll(List.of(blockPos.relative(Direction.EAST), blockPos.relative(Direction.WEST)));
                } else {
                    potentialPositions.addAll(List.of(blockPos.relative(Direction.NORTH), blockPos.relative(Direction.SOUTH)));
                }

                for (BlockPos potentialPos : potentialPositions) {
                    var state = level.getBlockState(potentialPos);
                    if (state.isAir() || state.getMaterial() == Material.WATER) {
                        level.setBlockAndUpdate(potentialPos, Blocks.TRAPPED_CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
                        if (level.getBlockEntity(potentialPos) instanceof ChestBlockEntity chest) {
                            chest.setItem(0, theStack);
                            chests.add(potentialPos);
                            break;
                        }
                    }
                }
            }
        }

        if (chests.isEmpty()) {
            quest.abandon(player);
            return;
        }

        if (Quests.showExploreHint) {
            player.displayClientMessage(new TranslatableComponent("gui.strange.quests.explore_nearby"), true);
            level.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.6F, 1.2F);
        }

        chests.forEach(chest -> LogHelper.debug(Strange.MOD_ID, getClass(), "Added loot to chest at pos: " + chest));
        quest.setDirty();
    }

    @Override
    public CompoundTag save() {
        var out = new CompoundTag();

        if (!items.isEmpty()) {
            var list = new ListTag();
            for (ItemStack stack : items) {
                var itemTag = new CompoundTag();
                stack.save(itemTag);
                list.add(itemTag);
            }

            out.put(ITEMS_TAG, list);
        }

        if (structurePos != null) {
            out.putLong(STRUCTURE_POS_TAG, structurePos.asLong());
        }

        if (!chests.isEmpty()) {
            var chestPositions = chests.stream()
                .map(BlockPos::immutable)
                .map(BlockPos::asLong)
                .toList();

            out.putLongArray(CHESTS_TAG, chestPositions);
        }

        if (dimension != null) {
            out.putString(DIMENSION_TAG, dimension.toString());
        }

        out.putInt(CHEST_START_TAG, chestStart);
        out.putInt(CHEST_RANGE_TAG, chestRange);

        return out;
    }

    @Override
    public void load(CompoundTag tag) {
        items.clear();
        chests.clear();

        chestRange = tag.getInt(CHEST_RANGE_TAG);
        chestStart = tag.getInt(CHEST_START_TAG);
        structurePos = tag.contains(STRUCTURE_TAG) ? BlockPos.of(tag.getLong(STRUCTURE_TAG)) : null;
        dimension = ResourceLocation.tryParse(tag.getString(DIMENSION_TAG));

        if (dimension == null) {
            dimension = Level.OVERWORLD.location(); // This is crap.
        }

        if (tag.contains(CHESTS_TAG)) {
            var chestPositions = tag.getLongArray(CHESTS_TAG);
            for (long pos : chestPositions) {
                chests.add(BlockPos.of(pos));
            }
        }

        var list = (ListTag) tag.get(ITEMS_TAG);
        if (list != null && list.size() > 0) {
            for (Tag itemTag : list) {
                var stack = ItemStack.of((CompoundTag)itemTag);
                items.add(stack);
            }
        }
    }

    @Override
    public void provideMap(ServerPlayer player) {
        var title = Quests.getTranslatedKey(quest.getDefinition(), "title");
        var map = MapHelper.create((ServerLevel) player.level, structurePos, title, MAP_TAG, MAP_COLOR);
        player.getInventory().placeItemBackInInventory(map);
    }
}
