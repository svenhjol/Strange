package svenhjol.strange.feature.runestones.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import svenhjol.charm.charmony.Log;
import svenhjol.charm.charmony.feature.FeatureResolver;
import svenhjol.strange.feature.runestones.Runestones;

import java.util.ArrayList;
import java.util.Arrays;

public class RunestoneTeleport implements FeatureResolver<Runestones> {
    public static final int PLAY_SOUND_TICKS = 5;
    public static final int TELEPORT_TICKS = 10;
    public static final int REPOSITION_TICKS = 20;

    private final ServerPlayer player;
    private final ServerLevel level;
    private final RunestoneBlockEntity runestone;
    private Vec3 target;
    private ResourceKey<Level> dimension;
    private boolean useExactPosition = false;
    private boolean teleportingInSameDimension = true;
    private boolean doneAdvancementsAndEffects = false;
    private boolean valid = false;
    private int ticks = 0;

    public RunestoneTeleport(ServerPlayer player, RunestoneBlockEntity runestone) {
        this.player = player;
        this.level = (ServerLevel)player.level();
        this.runestone = runestone;

        this.setTargetAndDimension();
        this.valid = true;
    }

    public Log log() {
        return feature().log();
    }

    public boolean isValid() {
        return valid;
    }

    public void tick() {
        if (!isValid()) {
            return;
        }

        ticks++;

        if (ticks == 1) {
            if (feature().dizzyEffect()) {
                // Adds dizziness effect to the teleporting player.
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, feature().protectionDuration() * 2, 5));
            }
        }

        if (ticks == PLAY_SOUND_TICKS && teleportingInSameDimension) {
            playTeleportSound();
        }

        if (ticks < TELEPORT_TICKS) return;

        if (ticks == TELEPORT_TICKS) {
            teleport();
            return;
        }

        if (ticks < REPOSITION_TICKS) return;

        if (player.isRemoved()) {
            valid = false;
            return;
        }

        reposition();
    }

    private void teleport() {
        log().debug("Called teleport for player " + player);
        var protectionTicks = feature().protectionDuration();

        // Add protection effects to the teleporting player.
        var effects = new ArrayList<>(Arrays.asList(
            new MobEffectInstance(MobEffects.FIRE_RESISTANCE, protectionTicks, 1),
            new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, protectionTicks, 1),
            new MobEffectInstance(MobEffects.REGENERATION, protectionTicks, 1)
        ));
        effects.forEach(player::addEffect);

        // Do the teleport to the location - repositioning comes later.
        if (!player.getAbilities().instabuild) {
            player.setInvulnerable(true);
        }

        if (!teleportingInSameDimension) {
            var server = level.getServer();
            var newDimension = server.getLevel(dimension);
            if (newDimension != null) {
                Helpers.changeDimension(player, newDimension, target);
                valid = true;
            }
            return;
        }

        player.teleportTo(target.x, target.y, target.z);
        valid = true;
    }

    private void reposition() {
        if (useExactPosition) {
            move(new BlockPos((int)target.x, (int)target.y, (int)target.z));
            return;
        }

        var level = player.level();
        var seaLevel = level.getSeaLevel();
        var pos = new BlockPos((int)target.x, seaLevel, (int)target.z);

        if (!level.dimensionType().hasCeiling()) {
            var surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
            log().debug("Found a valid surface position " + surface);
            var posBelow = surface.below();
            var stateBelow = level.getBlockState(posBelow);

            if (!(stateBelow.isAir() && !stateBelow.getFluidState().is(Fluids.LAVA))
                || stateBelow.getFluidState().is(Fluids.WATER)
                || stateBelow.getFluidState().is(Fluids.FLOWING_WATER)) {
                move(surface.above());
                return;
            } else {
                log().debug("Unable to place player on surface because state=" + stateBelow + ", falling back to checks");
            }
        } else {
            var surface = Helpers.getSurfacePos(level, pos, Math.min(seaLevel + 40, level.getHeight() - 20));

            if (surface != null) {
                move(surface);
                return;
            } else {
                log().debug("Unable to place player in an air space, falling back to checks");
            }
        }

        var mutable = new BlockPos.MutableBlockPos();
        mutable.set(pos.getX(), seaLevel + 24, pos.getZ());

        var surfaceBlock = getSurfaceBlockForDimension();
        var validFloor = false;
        var validCurrent = false;
        var validAbove = false;

        // Check blocks below for solid ground or water
        for (int tries = 0; tries < 48; tries++) {
            var above = level.getBlockState(mutable.above());
            var current = level.getBlockState(mutable);
            var below = level.getBlockState(mutable.below());

            validFloor = below.isSolidRender(level, mutable.below()) || level.isWaterAt(mutable.below());
            validCurrent = current.isAir() || level.isWaterAt(mutable);
            validAbove = above.isAir() || level.isWaterAt(mutable.above());

            if (validFloor && validCurrent && validAbove) {
                log().debug("Found valid calculated position "  + mutable + " after " + tries + " tries");
                move(mutable);
                return;
            }

            mutable.move(Direction.DOWN);
        }

        if (!validFloor) {
            makePlatform(mutable.below(), surfaceBlock);
        }
        if (!validCurrent) {
            level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 2);
            log().debug("Made air space at " + mutable);

            // Check each cardinal point for lava
            for (var direction : Arrays.asList(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
                var relative = mutable.relative(direction);
                var state = level.getBlockState(relative);

                if (state.is(Blocks.LAVA)) {
                    makeWall(relative, surfaceBlock);
                }
            }
        }
        if (!validAbove) {
            log().debug("Made air space at " + mutable);
            level.setBlock(mutable.above(), Blocks.AIR.defaultBlockState(), 2);
        }

        // Check that lava doesn't pour down into the new gap
        var relative = mutable.above(2);
        if (level.getBlockState(relative).is(Blocks.LAVA)) {
            makePlatform(relative, surfaceBlock);
        }

        move(mutable);
    }

    /**
     * Move the player into position after making the location safe.
     */
    private void move(BlockPos pos) {
        player.moveTo(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d);
        player.teleportTo(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d);

        if (!player.getAbilities().instabuild) {
            player.setInvulnerable(false);
        }
        
        doAdvancementsAndEffects();

        valid = false;
    }

    private void makeWall(BlockPos pos, BlockState state) {
        var level = player.level();
        level.setBlock(pos, state, 2);
        level.setBlock(pos.above(), state, 2);
        log().debug("Made wall at " + pos);
    }

    private void makePlatform(BlockPos pos, BlockState solid) {
        var level = player.level();
        var x = pos.getX();
        var y = pos.getY();
        var z = pos.getZ();

        BlockPos.betweenClosed(x - 1, y, z - 1, x + 1, y, z + 1).forEach(
            p -> level.setBlockAndUpdate(p, solid));
        log().debug("Made platform at " + pos);
    }

    private BlockState getSurfaceBlockForDimension() {
        var level = player.level();
        BlockState state;

        if (level.dimension() == Level.END) {
            state = Blocks.END_STONE.defaultBlockState();
        } else if (level.dimension() == Level.NETHER) {
            state = Blocks.NETHERRACK.defaultBlockState();
        } else {
            state = Blocks.STONE.defaultBlockState();
        }

        return state;
    }

    private void setTargetAndDimension() {
        if (Helpers.runestoneLinksToSpawnPoint(runestone)) {
            // Handle player spawn point runestone.
            this.dimension = player.getRespawnDimension();

            var respawnPosition = player.getRespawnPosition();
            if (respawnPosition != null) {
                this.useExactPosition = true;
                this.target = respawnPosition.getCenter();
            } else {
                this.target = level.getSharedSpawnPos().getCenter();
            }
        } else {
            // All standard runestones just use the same dimension and fixed target pos.
            this.dimension = level.dimension();
            this.target = runestone.target.getCenter();
        }
        
        this.teleportingInSameDimension = level.dimension() == dimension;
    }
    
    private void doAdvancementsAndEffects() {
        if (doneAdvancementsAndEffects) return; // Don't do this twice. Sometimes repositioning can call this a couple of times?
        
        // If it was dimensional travel, play the sound now.
        if (!teleportingInSameDimension) {
            playTeleportSound();
        }

        // Do advancements.
        feature().advancements.travelledViaRunestone(player);
        if (Helpers.runestoneLinksToSpawnPoint(runestone)) {
            feature().advancements.travelledHomeViaRunestone(player);
        }

        // Tell the client the location of where the player travelled to.
        Networking.S2CTeleportedLocation.send(player, runestone.location);
        
        doneAdvancementsAndEffects = true;
    }
    
    private void playTeleportSound() {
        log().debug("Playing teleport sound for player " + player);
        player.level().playSound(null, player.blockPosition(), feature().registers.travelSound.get(), SoundSource.BLOCKS);
    }

    @Override
    public Class<Runestones> typeForFeature() {
        return Runestones.class;
    }
}
