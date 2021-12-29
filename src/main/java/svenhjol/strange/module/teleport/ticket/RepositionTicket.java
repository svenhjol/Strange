package svenhjol.strange.module.teleport.ticket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.module.teleport.Teleport;
import svenhjol.strange.module.teleport.iface.ITicket;

import java.util.Arrays;

public class RepositionTicket implements ITicket {
    private final LivingEntity entity;
    private final ServerLevel level;
    private int ticks;
    private boolean valid;
    private boolean success;

    public RepositionTicket(LivingEntity entity) {
        this.entity = entity;
        this.level = (ServerLevel)entity.level;
        this.ticks = 0;
        this.valid = true;
        this.success = false;
    }

    @Override
    public void tick() {
        if (entity.isRemoved()) {
            LogHelper.debug(Strange.MOD_ID, this.getClass(), "Reposition ticket is no longer valid as the entity has been removed");
            valid = false;
        }

        if (!valid) return;

        if (ticks++ >= Teleport.repositionTicks) {
            LogHelper.debug(Strange.MOD_ID, this.getClass(), "Processing reposition ticket");
            BlockPos pos = entity.blockPosition();
            BlockState surfaceBlock = WorldHelper.getSurfaceBlockForDimension(level);

            if (!level.dimensionType().hasCeiling()) {
                BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);

                if (level.getBlockState(surfacePos.below()).isSolidRender(level, surfacePos.below())) {
                    entity.moveTo(surfacePos.getX() + 0.5D, surfacePos.getY() + 0.5D, surfacePos.getZ() + 0.5D);
                    entity.teleportTo(surfacePos.getX() + 0.5D, surfacePos.getY() + 0.5D, surfacePos.getZ() + 0.5D);
                    LogHelper.debug(Strange.MOD_ID, this.getClass(), "Found a valid surface position, moving entity to it: " + surfacePos);
                    success = true;
                    return;
                }
            }

            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            mutable.set(pos.getX(), pos.getY(), pos.getZ());
            boolean validFloor = false;
            boolean validCurrent = false;
            boolean validAbove = false;

            // check 8 blocks below for solid ground or water
            for (int tries = 0; tries < 8; tries++) {
                BlockState above = level.getBlockState(mutable.above());
                BlockState current = level.getBlockState(mutable);
                BlockState below = level.getBlockState(mutable.below());

                validFloor = below.isSolidRender(level, mutable.below()) || level.isWaterAt(mutable.below());
                validCurrent = current.isAir() || level.isWaterAt(mutable);
                validAbove = above.isAir() || level.isWaterAt(mutable.above());

                if (validFloor && validCurrent && validAbove) {
                    LogHelper.debug(Strange.MOD_ID, this.getClass(), "Found a valid calculated position after " + tries + " tries: " + mutable);
                    success = true;
                    return;
                }

                mutable.move(Direction.DOWN);
            }

            if (!validFloor) {
                makePlatform(pos.below(), surfaceBlock);
            }
            if (!validCurrent) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                LogHelper.debug(Strange.MOD_ID, this.getClass(), "Made air space at " + pos);

                // check each cardinal point for lava
                for (Direction direction : Arrays.asList(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
                    BlockPos relative = pos.relative(direction);
                    BlockState state = level.getBlockState(relative);
                    if (state.is(Blocks.LAVA)) {
                        setSolidWall(relative, surfaceBlock);
                    }
                }
            }
            if (!validAbove) {
                LogHelper.debug(Strange.MOD_ID, this.getClass(), "Made air space at " + pos);
                level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
            }

            // check that lava doesn't pour down into the new gap
            BlockPos relative = pos.above(2);
            if (level.getBlockState(relative).is(Blocks.LAVA)) {
                makePlatform(relative, surfaceBlock);
            }

            Teleport.noEndPlatform.remove(entity.getUUID());
            success = true;
        }
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean isSuccessful() {
        return success;
    }

    @Override
    public LivingEntity getEntity() {
        return entity;
    }

    private void setSolidWall(BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
        level.setBlock(pos.above(), state, 2);
        LogHelper.debug(Strange.MOD_ID, this.getClass(), "Made wall at: " + pos);
    }

    private void makePlatform(BlockPos pos, BlockState solid) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        BlockPos.betweenClosed(x - 1, y, z - 1, x + 1, y, z + 1).forEach(p -> level.setBlockAndUpdate(p, solid));
        LogHelper.debug(Strange.MOD_ID, this.getClass(), "Made platform at: " + pos);
    }
}
