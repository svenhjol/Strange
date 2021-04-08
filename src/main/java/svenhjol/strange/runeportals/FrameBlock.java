package svenhjol.strange.runeportals;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.PlayerHelper;
import svenhjol.strange.runestones.Runestones;
import svenhjol.strange.runestones.RunestonesHelper;
import svenhjol.strange.runestones.RunicFragmentItem;

import java.util.ArrayList;
import java.util.List;

public class FrameBlock extends BaseFrameBlock {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final IntProperty RUNE = IntProperty.of("rune", 0, RunestonesHelper.NUMBER_OF_RUNES - 1);

    protected static final VoxelShape EAST_FRAME_SHAPE;
    protected static final VoxelShape EAST_RUNE_SHAPE;
    protected static final VoxelShape WEST_FRAME_SHAPE;
    protected static final VoxelShape WEST_RUNE_SHAPE;
    protected static final VoxelShape NORTH_FRAME_SHAPE;
    protected static final VoxelShape NORTH_RUNE_SHAPE;
    protected static final VoxelShape SOUTH_FRAME_SHAPE;
    protected static final VoxelShape SOUTH_RUNE_SHAPE;
    protected static final VoxelShape EAST_SHAPE;
    protected static final VoxelShape WEST_SHAPE;
    protected static final VoxelShape NORTH_SHAPE;
    protected static final VoxelShape SOUTH_SHAPE;

    public FrameBlock(CharmModule module) {
        super(module, "frame", AbstractBlock.Settings.copy(Blocks.STONE));
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(FACING) == Direction.WEST) {
            return WEST_SHAPE;
        } else if (state.get(FACING) == Direction.SOUTH) {
            return SOUTH_SHAPE;
        } else if (state.get(FACING) == Direction.EAST) {
            return EAST_SHAPE;
        } else {
            return NORTH_SHAPE;
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        RunePortals.breakSurroundingPortals(world, pos);
        super.onBreak(world, pos, state, player);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack held = player.getStackInHand(hand);
        Integer runeValue = state.get(FrameBlock.RUNE);

        if (world.isClient)
            return ActionResult.PASS;

        if (player.isSneaking()) {
            PlayerHelper.addOrDropStack(player, new ItemStack(Runestones.RUNIC_FRAGMENTS.get(runeValue)));
            world.setBlockState(pos, RunePortals.WOLF_BLOCK.getDefaultState(), 3);
            RunePortals.breakSurroundingPortals(world, pos);
            return ActionResult.CONSUME;
        }

        if (held.getItem() instanceof FlintAndSteelItem) {
            boolean result = tryActivate((ServerWorld)world, pos, state);
            if (result)
                return ActionResult.CONSUME;
        }
        if (held.getItem() instanceof RunicFragmentItem) {
            ActionResult result = super.onUse(state, world, pos, player, hand, hit);

            if (result == ActionResult.CONSUME) {
                // rune was successfully applied; drop the original as an item
                if (!player.getAbilities().creativeMode)
                    PlayerHelper.addOrDropStack(player, new ItemStack(Runestones.RUNIC_FRAGMENTS.get(runeValue)));
            }

            RunePortals.breakSurroundingPortals(world, pos);
        }

        return ActionResult.PASS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, RUNE);
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state
            .with(FACING, rotation.rotate(state.get(FACING)))
            .with(RUNE, 0);
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    public boolean tryActivate(ServerWorld world, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof FrameBlock) {
            Axis axis;
            List<Integer> order = new ArrayList<>();

            if (!world.getBlockState(pos.up()).isAir())
                return false;

            // TODO: pos shifting to find the correct starting pos

            if (world.getBlockState(pos.east()).getBlock() instanceof FrameBlock
                && world.getBlockState(pos.west()).getBlock() instanceof FrameBlock
            ) {
                axis = Axis.X;
            } else if (world.getBlockState(pos.north()).getBlock() instanceof FrameBlock
                && world.getBlockState(pos.south()).getBlock() instanceof FrameBlock
            ) {
                axis = Axis.Z;
            } else {
                return false;
            }

            switch (axis) {
                case X:
                    final BlockState eastState = world.getBlockState(pos.east(2).up(1));
                    final BlockState westState = world.getBlockState(pos.west(2).up(1));

                    if (!(eastState.getBlock() instanceof FrameBlock) || !(westState.getBlock() instanceof FrameBlock))
                        return false;

                    if (eastState.get(FrameBlock.FACING) == Direction.NORTH) {
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.east(2).up(i + 1), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.east(1 - i).up(4), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.west(2).up(3 - i), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.west(1 - i), order)) return false;
                        }
                    } else if (westState.get(FrameBlock.FACING) == Direction.SOUTH) {
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.west(2).up(i + 1), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.west(1 - i).up(4), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.east(2).up(3 - i), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.east(1 - i), order)) return false;
                        }
                    }

                    break;

                case Z:
                    final BlockState northState = world.getBlockState(pos.north(2).up(1));
                    final BlockState southState = world.getBlockState(pos.south(2).up(1));

                    if (!(northState.getBlock() instanceof FrameBlock) || !(southState.getBlock() instanceof FrameBlock))
                        return false;

                    if (northState.get(FrameBlock.FACING) == Direction.WEST) {
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.north(2).up(i + 1), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.north(1 - i).up(4), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.south(2).up(3 - i), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.south(1 - i), order)) return false;
                        }
                    } else if (southState.get(FrameBlock.FACING) == Direction.EAST) {
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.south(2).up(i + 1), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.south(1 - i).up(4), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.north(2).up(3 - i), order)) return false;
                        }
                        for (int i = 0; i < 3; i++) {
                            if (!addOrder(world, pos.north(1 - i), order)) return false;
                        }
                    }

                    break;

                default:
                    return false;
            }

            if (order.size() == 12) {
                Charm.LOG.info("Order: " + order.toString());
                int orientation = axis == Axis.X ? 0 : 1;

                StringBuilder build = new StringBuilder();
                for (int i = 0; i < order.size(); i++) {
                    int r = order.get(i);
                    build.append(r);
                }
                long hash = Long.parseLong(build.toString().substring(0, build.length() / 2));

                for (int a = -1; a < 2; a++) {
                    for (int b = 1; b < 4; b++) {
                        BlockPos p = axis == Axis.X ? pos.add(a, b, 0) : pos.add(0, b, a);
                        world.setBlockState(p, RunePortals.RUNE_PORTAL_BLOCK.getDefaultState().with(RunePortalBlock.AXIS, axis), 3);
                        setPortal(world, p, order, orientation, hash);
                    }
                }
                return true;
            }
        }

        return false;
    }

    private void setPortal(World world, BlockPos pos, List<Integer> order, int orientation, long hash) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null)
            return;

        RunePortalBlockEntity portal = (RunePortalBlockEntity)blockEntity;
        portal.orientation = orientation;
        portal.color = DyeColor.BLUE.getId();
        portal.markDirty();

        // TODO: handle linking in this method
//        BlockState state = world.getBlockState(pos);
        world.getBlockTickScheduler().schedule(pos, this, 2);
    }

    private boolean addOrder(ServerWorld world, BlockPos pos, List<Integer> order) {
        final BlockState s = world.getBlockState(pos);
        if (!(s.getBlock() instanceof FrameBlock))
            return false;

        order.add(s.get(FrameBlock.RUNE));
        return true;
    }

    static {
        EAST_FRAME_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 13.0D, 16.0D, 16.0D);
        EAST_RUNE_SHAPE = Block.createCuboidShape(12.0D, 4.0D, 4.0D, 16.0D, 12.0D, 12.0D);

        WEST_FRAME_SHAPE = Block.createCuboidShape(3.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        WEST_RUNE_SHAPE = Block.createCuboidShape(0.0D, 4.0D, 4.0D, 3.0D, 12.0D, 12.0D);

        NORTH_FRAME_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 3.0D, 16.0D, 16.0D, 16.0D);
        NORTH_RUNE_SHAPE = Block.createCuboidShape(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 3.0D);

        SOUTH_FRAME_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 13.0D);
        SOUTH_RUNE_SHAPE = Block.createCuboidShape(4.0D, 4.0D, 13.0D, 12.0D, 12.0D, 16.0D);

        EAST_SHAPE = VoxelShapes.union(EAST_FRAME_SHAPE, EAST_RUNE_SHAPE);
        WEST_SHAPE = VoxelShapes.union(WEST_FRAME_SHAPE, WEST_RUNE_SHAPE);
        NORTH_SHAPE = VoxelShapes.union(NORTH_FRAME_SHAPE, NORTH_RUNE_SHAPE);
        SOUTH_SHAPE = VoxelShapes.union(SOUTH_FRAME_SHAPE, SOUTH_RUNE_SHAPE);
    }
}
