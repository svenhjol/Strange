package svenhjol.strange.feature.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.Structure;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.helper.TagHelper;
import svenhjol.charmony_api.event.EntityJoinEvent;
import svenhjol.charmony_api.event.PlayerTickEvent;
import svenhjol.charmony_api.event.ServerStartEvent;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.travel_journal.TravelJournal;

import java.util.*;
import java.util.function.Supplier;

public class Runestones extends CommonFeature {
    static final String STONE_ID = "stone_runestone";
    static final String BLACKSTONE_ID = "blackstone_runestone";
    static final String OBSIDIAN_ID = "obsidian_runestone";
    public static Supplier<RunestoneBlock> stoneBlock;
    public static Supplier<RunestoneBlock> blackstoneBlock;
    public static Supplier<RunestoneBlock> obsidianBlock;
    static Supplier<BlockEntityType<RunestoneBlockEntity>> blockEntity;
    static final List<IRunestoneDefinition> DEFINITIONS = new ArrayList<>();
    static final Map<RunestoneBlock, IRunestoneDefinition> BLOCK_DEFINITIONS = new HashMap<>();
    static final List<Supplier<RunestoneBlock.BlockItem>> BLOCK_ITEMS = new LinkedList<>();
    static final Map<UUID, RunestoneTeleport> TELEPORTS = new HashMap<>();
    static Supplier<SoundEvent> travelSound;
    static Supplier<SoundEvent> activateSound;

    public static boolean dizzyEffect = true;

    public static int protectionDuration = 3;

    @Override
    public void register() {
        var registry = mod().registry();

        RunestonesNetwork.register(registry);

        blockEntity = registry.blockEntity("runestone", () -> RunestoneBlockEntity::new);
        activateSound = registry.soundEvent("runestone_activate");
        travelSound = registry.soundEvent("runestone_travel");

        stoneBlock = registry.block(STONE_ID, RunestoneBlock::new);
        blackstoneBlock = registry.block(BLACKSTONE_ID, RunestoneBlock::new);
        obsidianBlock = registry.block(OBSIDIAN_ID, RunestoneBlock::new);

        BLOCK_ITEMS.add(registry.item(STONE_ID, () -> new RunestoneBlock.BlockItem(stoneBlock)));
        BLOCK_ITEMS.add(registry.item(BLACKSTONE_ID, () -> new RunestoneBlock.BlockItem(blackstoneBlock)));
        BLOCK_ITEMS.add(registry.item(OBSIDIAN_ID, () -> new RunestoneBlock.BlockItem(obsidianBlock)));

        RunestoneDefinitions.init();
    }

    @Override
    public void runWhenEnabled() {
        ServerStartEvent.INSTANCE.handle(this::handleServerStart);
        PlayerTickEvent.INSTANCE.handle(this::handlePlayerTick);
        EntityJoinEvent.INSTANCE.handle(this::handleEntityJoin);
    }

    @SuppressWarnings("unchecked")
    public static void prepareRunestone(LevelAccessor level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }

        if (!(level.getBlockEntity(pos) instanceof RunestoneBlockEntity runestone)) {
            return;
        }

        var log = Mods.common(Strange.ID).log();
        var state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RunestoneBlock block)) {
            return;
        }

        if (!Runestones.BLOCK_DEFINITIONS.containsKey(block)) {
            log.warn(RunestoneBlock.class, "No definition found for runestone block " + block + " at pos " + pos);
            return;
        }

        var random = RandomSource.create(pos.asLong());
        var definition = Runestones.BLOCK_DEFINITIONS.get(block);
        var opt = definition.getDestination(level, pos, random);
        var registryAccess = level.registryAccess();

        if (opt.isEmpty()) {
            log.warn(RunestoneBlock.class, "Failed to run getDestination on runestone at " + pos);
            return;
        }

        var tag = opt.get();
        if (tag.registry() == Registries.BIOME) {

            var biomeTag = (TagKey<Biome>) tag;
            var biomeRegistry = registryAccess.registryOrThrow(biomeTag.registry());
            var destinations = TagHelper.getValues(biomeRegistry, biomeTag)
                .stream().map(biomeRegistry::getKey).toList();

            if (destinations.isEmpty()) {
                log.warn(RunestoneBlock.class, "Empty biome destinations for runestone at pos " + pos);
                return;
            }

            runestone.destination = destinations.get(random.nextInt(destinations.size()));
            runestone.type = DestinationType.BIOME;
            log.debug(RunestoneBlock.class, "Set biome " + runestone.destination + " for runestone at pos " + pos);

        } else if (tag.registry() == Registries.STRUCTURE) {

            var structureTag = (TagKey<Structure>) tag;
            var structureRegistry = registryAccess.registryOrThrow(structureTag.registry());
            var destinations = TagHelper.getValues(structureRegistry, structureTag)
                .stream().map(structureRegistry::getKey).toList();

            if (destinations.isEmpty()) {
                log.warn(RunestoneBlock.class, "Empty structure destinations for runestone at pos " + pos);
                return;
            }

            runestone.destination = destinations.get(random.nextInt(destinations.size()));
            runestone.type = DestinationType.STRUCTURE;
            log.debug(RunestoneBlock.class, "Set structure " + runestone.destination + " for runestone at pos " + pos);

        }

        runestone.setChanged();

        random.nextInt();
        var directions = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
        state = state.setValue(RunestoneBlock.FACING, directions.get(random.nextInt(directions.size())));
        level.setBlock(pos, state, 2);
    }

    public static boolean tryLocate(ServerLevel level, RunestoneBlockEntity runestone) {
        var log = Mods.common(Strange.ID).log();
        var pos = runestone.getBlockPos();
        var random = RandomSource.create(pos.asLong());
        var target = RunestoneHelper.addRandomOffset(level, pos, random, 1000, 2000);
        var registryAccess = level.registryAccess();

        switch (runestone.type) {
            case BIOME -> {
                var result = level.findClosestBiome3d(x -> x.is(runestone.destination), target, 6400, 32, 64);
                if (result == null) {
                    log.warn(Runestones.class, "Could not locate biome for " + runestone.destination);
                    return false;
                }

                runestone.target = result.getFirst();
            }
            case STRUCTURE -> {
                var structureRegistry = registryAccess.registryOrThrow(Registries.STRUCTURE);
                var structure = structureRegistry.get(runestone.destination);
                if (structure == null) {
                    log.warn(Runestones.class, "Could not get registered structure for " + runestone.destination);
                    return false;
                }

                // Wrap structure in holder and holderset so that it's in the right format for find
                var set = HolderSet.direct(Holder.direct(structure));
                var result = level.getChunkSource().getGenerator()
                    .findNearestMapStructure(level, set, target, 100, false);

                if (result == null) {
                    log.warn(Runestones.class, "Could not locate structure for " + runestone.destination);
                    return false;
                }

                runestone.target = result.getFirst();
            }
            default -> {
                log.warn(Runestones.class, "Not a valid destination type for runestone at " + pos);
                return false;
            }
        }

        runestone.setChanged();
        return true;
    }

    public static boolean tryTeleport(ServerPlayer player, RunestoneBlockEntity runestone) {
        runestone.discovered = player.getScoreboardName();
        runestone.setChanged();

        TravelJournal.getLearned(player).ifPresent(
            learned -> learned.learn(runestone.destination));
        TravelJournal.sync(player);

        var teleport = new RunestoneTeleport(player, runestone.target);
        teleport.teleport();
        Runestones.TELEPORTS.put(player.getUUID(), teleport);

        return true;
    }

    /**
     * Send the level seed to the logged-in player.
     */
    private void handleEntityJoin(Entity entity, Level level) {
        if (entity instanceof ServerPlayer player) {
            var serverLevel = (ServerLevel)level;
            var seed = serverLevel.getSeed();
            RunestonesNetwork.SentLevelSeed.send(player, seed);
            RunestoneHelper.CACHED_RUNIC_NAMES.clear();
        }
    }

    /**
     * Register a runestone definition. This is required for runestone behavior to function correctly.
     * Adds the definition block reference to the runestone block entity.
     */
    public static void registerDefinition(IRunestoneDefinition definition) {
        var registry = Mods.common(Strange.ID).registry();

        DEFINITIONS.add(definition);
        registry.blockEntityBlocks(blockEntity, List.of(definition.block()));
    }

    /**
     * When server starts, create a map of runestone blocks to registered runestone definitions.
     */
    private void handleServerStart(MinecraftServer server) {
        BLOCK_DEFINITIONS.clear();
        DEFINITIONS.forEach(definition -> BLOCK_DEFINITIONS.put(definition.block().get(), definition));
    }

    private void handlePlayerTick(Player player) {
        var uuid = player.getUUID();

        if (TELEPORTS.containsKey(uuid)) {
            var teleport = TELEPORTS.get(uuid);
            if (teleport.isValid()) {
                teleport.tick();
            } else {
                TELEPORTS.remove(uuid);
            }
        }
    }
}
