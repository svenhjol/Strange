package svenhjol.strange.feature.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.iface.ILog;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.Arrays;

public class RunestoneTeleport {
    static final int PROCESS_AFTER = 10;
    static final int INVALIDATE_AFTER = 100;
    private final ServerPlayer player;
    private final Vec3 target;
    private final ILog log;
    private int ticks = 0;
    private boolean valid = false;

    public RunestoneTeleport(ServerPlayer player, BlockPos target) {
        this.player = player;
        this.target = target.getCenter();
        this.log = Mods.common(Strange.ID).log();
    }

    public void tick() {
        if (!isValid()) {
            return;
        }

        ticks++;

        if (ticks < PROCESS_AFTER) {
            return;
        }

        if (ticks > INVALIDATE_AFTER) {
            log.warn(getClass(), "Teleport process timed out for " + player);
            valid = false;
            return;
        }

        if (player.isRemoved()) {
            valid = false;
            return;
        }

        var level = player.level();
        var pos = player.blockPosition();
        var seaLevel = level.getSeaLevel();

        if (!level.dimensionType().hasCeiling()) {
            var surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
            log.debug(getClass(), "Found a valid surface position " + surface);

            if (level.getBlockState(surface.below()).isSolidRender(level, surface.below())) {
                reposition(surface);
                return;
            } else {
                log.debug(getClass(), "Unable to place player on surface, falling back to checks");
            }
        } else {
            var surface = RunestoneHelper.getSurfacePos(level, pos, Math.min(seaLevel + 40, level.getHeight() - 20));

            if (surface != null) {
                reposition(surface);
                return;
            } else {
                log.debug(getClass(), "Unable to place player in an air space, falling back to checks");
            }
        }

        var mutable = new BlockPos.MutableBlockPos();
        mutable.set(pos.getX(), seaLevel + 12, pos.getZ());

        var surfaceBlock = getSurfaceBlockForDimension();
        var validFloor = false;
        var validCurrent = false;
        var validAbove = false;

        // Check blocks below for solid ground or water
        for (int tries = 0; tries < 24; tries++) {
            var above = level.getBlockState(mutable.above());
            var current = level.getBlockState(mutable);
            var below = level.getBlockState(mutable.below());

            validFloor = below.isSolidRender(level, mutable.below()) || level.isWaterAt(mutable.below());
            validCurrent = current.isAir() || level.isWaterAt(mutable);
            validAbove = above.isAir() || level.isWaterAt(mutable.above());

            if (validFloor && validCurrent && validAbove) {
                log.debug(getClass(), "Found valid calculated position "  + mutable + " after " + tries + " tries");
                reposition(mutable);
                return;
            }

            mutable.move(Direction.DOWN);
        }

        if (!validFloor) {
            makePlatform(mutable.below(), surfaceBlock);
        }
        if (!validCurrent) {
            level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 2);
            log.debug(getClass(), "Made air space at " + mutable);

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
            log.debug(getClass(), "Made air space at " + mutable);
            level.setBlock(mutable.above(), Blocks.AIR.defaultBlockState(), 2);
        }

        // Check that lava doesn't pour down into the new gap
        var relative = mutable.above(2);
        if (level.getBlockState(relative).is(Blocks.LAVA)) {
            makePlatform(relative, surfaceBlock);
        }

        reposition(mutable);
    }

    public void teleport() {
        // Add protection and dizziness effects to the teleporting player.
        var effects = new ArrayList<>(Arrays.asList(
            new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Runestones.protectionDuration, 1),
            new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Runestones.protectionDuration, 1)
        ));
        if (Runestones.dizzyEffect) {
            effects.add(new MobEffectInstance(MobEffects.CONFUSION, Runestones.protectionDuration, 2));
        }
        effects.forEach(player::addEffect);

        // Play teleportation sound.
        player.level().playSound(null, player.blockPosition(), Runestones.travelSound.get(), SoundSource.BLOCKS, 1.0f, 1.0f);

        // Do the teleport to the location - repositioning comes later.
        player.teleportToWithTicket(target.x, target.y, target.z);
        valid = true;
    }

    private void reposition(BlockPos pos) {
        player.moveTo(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d);
        player.teleportTo(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d);
        valid = false;
    }

    private void makeWall(BlockPos pos, BlockState state) {
        var level = player.level();
        level.setBlock(pos, state, 2);
        level.setBlock(pos.above(), state, 2);
        log.debug(getClass(), "Made wall at " + pos);
    }

    private void makePlatform(BlockPos pos, BlockState solid) {
        var level = player.level();
        var x = pos.getX();
        var y = pos.getY();
        var z = pos.getZ();

        BlockPos.betweenClosed(x - 1, y, z - 1, x + 1, y, z + 1).forEach(
            p -> level.setBlockAndUpdate(p, solid));
        log.debug(getClass(), "Made platform at " + pos);
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

    public boolean isValid() {
        return valid;
    }
}
