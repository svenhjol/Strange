package svenhjol.strange.feature.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony_api.event.EntityJoinEvent;
import svenhjol.charmony_api.event.PlayerTickEvent;
import svenhjol.charmony_api.event.ServerStartEvent;
import svenhjol.strange.Strange;
import svenhjol.strange.StrangeTags;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class Runestones extends CommonFeature {
    static final String STONE_ID = "stone_runestone";
    static final String BLACKSTONE_ID = "blackstone_runestone";
    static Supplier<RunestoneBlock> stoneBlock;
    static Supplier<RunestoneBlock.BlockItem> stoneBlockItem;
    static Supplier<RunestoneBlock> blackstoneBlock;
    static Supplier<RunestoneBlock.BlockItem> blackstoneBlockItem;
    static Supplier<BlockEntityType<RunestoneBlockEntity>> blockEntity;
    static Supplier<SoundEvent> travelSound;
    static final List<IRunestoneDefinition> DEFINITIONS = new ArrayList<>();
    static final Map<RunestoneBlock, IRunestoneDefinition> BLOCK_DEFINITIONS = new HashMap<>();
    static final Map<UUID, RunestoneTeleport> TELEPORTS = new HashMap<>();

    public static boolean dizzyEffect = true;

    public static int protectionDuration = 5;

    @Override
    public void register() {
        var registry = mod().registry();

        RunestonesNetwork.register(registry);

        blockEntity = registry.blockEntity("runestone", () -> RunestoneBlockEntity::new);
        travelSound = registry.soundEvent("runestone_travel");

        stoneBlock = registry.block(STONE_ID, RunestoneBlock::new);
        stoneBlockItem = registry.item(STONE_ID, () -> new RunestoneBlock.BlockItem(stoneBlock));
        blackstoneBlock = registry.block(BLACKSTONE_ID, RunestoneBlock::new);
        blackstoneBlockItem = registry.item(BLACKSTONE_ID, () -> new RunestoneBlock.BlockItem(blackstoneBlock));

        // Register overworld stone runestone.
        registerDefinition(new IRunestoneDefinition() {
            @Override
            public Supplier<RunestoneBlock> block() {
                return stoneBlock;
            }

            @Override
            public BiFunction<Level, BlockPos, Optional<TagKey<?>>> getDestination() {
                return (level, pos) -> {
                    var random = level.getRandom();
                    TagKey<?> tag;

                    if (random.nextDouble() < 0.25d) {
                        tag = StrangeTags.OVERWORLD_RUNESTONE_BIOMES;
                    } else {
                        tag = StrangeTags.OVERWORLD_RUNESTONE_STRUCTURES;
                    }

                    return Optional.of(tag);
                };
            }
        });

        // Register nether blackstone runestone.
        registerDefinition(new IRunestoneDefinition() {
            @Override
            public Supplier<RunestoneBlock> block() {
                return blackstoneBlock;
            }

            @Override
            public BiFunction<Level, BlockPos, Optional<TagKey<?>>> getDestination() {
                return (level, pos) -> {
                    var random = level.getRandom();
                    TagKey<?> tag;

                    if (random.nextDouble() < 0.33d) {
                        tag = StrangeTags.NETHER_RUNESTONE_BIOMES;
                    } else {
                        tag = StrangeTags.NETHER_RUNESTONE_STRUCTURES;
                    }

                    return Optional.of(tag);
                };
            }
        });
    }

    @Override
    public void runWhenEnabled() {
        ServerStartEvent.INSTANCE.handle(this::handleServerStart);
        PlayerTickEvent.INSTANCE.handle(this::handlePlayerTick);
        EntityJoinEvent.INSTANCE.handle(this::handleEntityJoin);
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
