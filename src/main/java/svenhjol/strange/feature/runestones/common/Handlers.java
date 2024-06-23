package svenhjol.strange.feature.runestones.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import svenhjol.charm.charmony.feature.FeatureHolder;
import svenhjol.charm.charmony.helper.PlayerHelper;
import svenhjol.strange.api.iface.RunestoneDefinition;
import svenhjol.strange.feature.runestones.Runestones;

import java.util.*;

@SuppressWarnings("unused")
public final class Handlers extends FeatureHolder<Runestones> {
    public static final int MAX_SACRIFICE_CHECKS = 8;
    public static final int SACRIFICE_CHECK_TICKS = 10;

    public final Map<Block, List<RunestoneDefinition>> definitions = new HashMap<>();
    public final Map<UUID, RunestoneTeleport> teleports = new HashMap<>();

    public Handlers(Runestones feature) {
        super(feature);
    }

    public void lookingAtRunestoneReceived(Player player, Networking.C2SPlayerLooking packet) {
        var level = player.level();
        var pos = packet.pos();
        
        // If it's a valid runestone that the player is looking at, do the advancement.
        if (level.getBlockEntity(pos) instanceof RunestoneBlockEntity runestone
            && runestone.isValid()) {
            feature().advancements.lookedAtRunestone(player);
        }
    }
    
    /**
     * Reload all the provider definitions into a map of runestone block -> runestone definition.
     */
    public void serverStart(MinecraftServer server) {
        definitions.clear();
        for (var definition : feature().providers.definitions) {
            this.definitions.computeIfAbsent(definition.runestoneBlock().get(), a -> new ArrayList<>()).add(definition);
        }
    }

    public void entityJoin(Entity entity, Level level) {
        if (entity instanceof ServerPlayer player) {
            var serverLevel = (ServerLevel)level;
            var seed = serverLevel.getSeed();
            Networking.S2CWorldSeed.send(player, seed);
        }
    }

    public boolean enderpearlImpact(ThrownEnderpearl enderpearl, HitResult hitResult) {
        var type = hitResult.getType();
        var owner = enderpearl.getOwner();
        var level = enderpearl.level();

        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        if (!(owner instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        if (type == HitResult.Type.BLOCK) {
            var blockHitResult = (BlockHitResult)hitResult;
            var pos = blockHitResult.getBlockPos();

            if (!(level.getBlockEntity(pos) instanceof RunestoneBlockEntity runestone)) {
                return false;
            }
            if (!runestone.isActivated()) {
                return false;
            }
            if (!runestone.isValid()) {
                // That sounds painful.
                explode(level, pos);
                return false;
            }

            enderpearl.discard();
            tryTeleportPlayer(serverPlayer, runestone);
            return true;
        }

        return false;
    }

    public void playerTick(Player player) {
        var uuid = player.getUUID();

        if (teleports.containsKey(uuid)) {
            var teleport = teleports.get(uuid);
            if (teleport.isValid()) {
                teleport.tick();
            } else {
                log().debug("Removing completed teleport for " + uuid);
                teleports.remove(uuid);
            }
        }
    }

    public void tickRunestone(ServerLevel level, BlockPos pos, BlockState state, RunestoneBlockEntity runestone) {
        if (!runestone.isActivated() && runestone.isValid() && level.getGameTime() % SACRIFICE_CHECK_TICKS == 0) {
            ItemEntity foundItem = null;
            var itemEntities = level.getEntitiesOfClass(ItemEntity.class, (new AABB(pos)).inflate(4.8d));
            for (var itemEntity : itemEntities) {
                if (itemEntity.getItem().is(runestone.sacrifice.getItem())) {
                    foundItem = itemEntity;
                    break;
                }
            }
            if (foundItem != null) {
                if (runestone.sacrificeChecks == 0) {
                    // Don't allow item to be picked up until the ritual is complete...
                    foundItem.setPickUpDelay(MAX_SACRIFICE_CHECKS * SACRIFICE_CHECK_TICKS);

                    // Start the powerup sound.
                    level.playSound(null, pos, feature().registers.powerUpSound.get(), SoundSource.BLOCKS);
                    level.playSound(null, BlockPos.containing(foundItem.position()), feature().registers.fizzleItemSound.get(), SoundSource.PLAYERS, 0.5f, 1.0f);
                }

                var itemPos = foundItem.position();

                // Add particle effect around the item to be consumed. This needs to be done via network packet.
                PlayerHelper.getPlayersInRange(level, pos, 8.0d)
                    .forEach(player -> Networking.S2CActivationWarmup.send((ServerPlayer)player, pos, itemPos));

                // Increase the number of checks. If maximum, consume the item and activate the runestone.
                runestone.sacrificeChecks++;
                if (runestone.sacrificeChecks >= MAX_SACRIFICE_CHECKS) {
                    var stack = foundItem.getItem();
                    if (stack.getCount() > 1) {
                        stack.shrink(1);
                    } else {
                        foundItem.discard();
                    }
                    runestone.activate(level, pos, state);
                }
            } else {
                runestone.sacrificeChecks = 0;
            }
        }
    }

    public void prepareRunestone(LevelAccessor level, BlockPos pos) {
        if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof RunestoneBlockEntity runestone)) {
            return;
        }

        var state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RunestoneBlock block)) {
            return;
        }

        if (!definitions.containsKey(block)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            log().error("No definition found for runestone block " + block + " at pos " + pos);
            return;
        }

        var random = RandomSource.create(pos.asLong());
        var blockDefinitions = new ArrayList<>(definitions.get(block));
        Collections.shuffle(blockDefinitions, new Random(random.nextLong()));

        for (var blockDefinition : blockDefinitions) {
            var location = blockDefinition.location(level, pos, random);
            random.nextInt(15335251);

            if (location.isPresent()) {
                var sacrifice = blockDefinition.sacrifice(level, pos, random).get();

                runestone.location = location.get();
                runestone.sacrifice = new ItemStack(sacrifice);

                log().debug("Set runestone location = " + runestone.location.id() + ", sacrifice = " + runestone.sacrifice.toString() + " at pos " + pos);
                runestone.setChanged();
                return;
            }
        }

        // Get a base block from the definitions to replace the runestone with.
        var baseBlock = !blockDefinitions.isEmpty() ? blockDefinitions.getFirst().baseBlock().get() : Blocks.AIR;
        level.setBlock(pos, baseBlock.defaultBlockState(), 2);
        log().debug("Could not resolve a location from runestone at pos " + pos + ", set to base block");
    }

    public void doActivationEffects(ServerLevel level, BlockPos pos) {
        // Lightning is cool.
        var bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(Vec3.atBottomCenterOf(pos.above()));
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }

        // Activation effect and advancement for all nearby players.
        PlayerHelper.getPlayersInRange(level, pos, 8.0d)
            .forEach(player -> {
                Networking.S2CActivation.send((ServerPlayer)player, pos);
                feature().advancements.activatedRunestone(player);
            });

        level.playSound(null, pos, feature().registers.activateSound.get(), SoundSource.BLOCKS, 1.15f, 1.0f);
    }

    public boolean trySetLocation(ServerLevel level, RunestoneBlockEntity runestone) {
        var pos = runestone.getBlockPos();
        var random = RandomSource.create(pos.asLong());
        var target = Helpers.addRandomOffset(level, pos, random, 1000, 2000);
        var registryAccess = level.registryAccess();

        switch (runestone.location.type()) {
            case BIOME -> {
                var result = level.findClosestBiome3d(x -> x.is(runestone.location.id()), target, 6400, 32, 64);
                if (result == null) {
                    log().warn("Could not locate biome for " + runestone.location.id());
                    return false;
                }

                runestone.target = result.getFirst();
            }
            case STRUCTURE -> {
                var structureRegistry = registryAccess.registryOrThrow(Registries.STRUCTURE);
                var structure = structureRegistry.get(runestone.location.id());
                if (structure == null) {
                    log().warn("Could not get registered structure for " + runestone.location.id());
                    return false;
                }

                // Wrap structure in holder and holderset so that it's in the right format for finding.
                var set = HolderSet.direct(Holder.direct(structure));
                var result = level.getChunkSource().getGenerator()
                    .findNearestMapStructure(level, set, target, 100, false);

                if (result == null) {
                    log().warn("Could not locate structure for " + runestone.location.id());
                    return false;
                }

                runestone.target = result.getFirst();
            }
            case PLAYER -> {
                if (Helpers.runestoneLinksToSpawnPoint(runestone)) {
                    runestone.target = null; // Player targets are dynamic.
                }
            }
            default -> {
                log().warn("Not a valid destination type for runestone at " + pos);
                return false;
            }
        }

        runestone.setChanged();
        return true;
    }

    public void explode(Level level, BlockPos pos) {
        level.explode(null, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, 1, Level.ExplosionInteraction.BLOCK);
        level.removeBlock(pos, false);
    }

    public void tryTeleportPlayer(ServerPlayer player, RunestoneBlockEntity runestone) {
        runestone.discovered = player.getScoreboardName();
        runestone.setChanged();

        // TODO: add player knowledge attribute

        var teleport = new RunestoneTeleport(player, runestone);
        teleports.put(player.getUUID(), teleport);
    }
}
