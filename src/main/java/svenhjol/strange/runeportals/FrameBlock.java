package svenhjol.strange.runeportals;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
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
import svenhjol.charm.base.block.CharmBlock;
import svenhjol.charm.base.helper.PlayerHelper;
import svenhjol.strange.runestones.Runestones;
import svenhjol.strange.runestones.RunestonesHelper;
import svenhjol.strange.runestones.RunicFragmentItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FrameBlock extends CharmBlock {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final IntProperty RUNE = IntProperty.of("rune", 0, RunestonesHelper.NUMBER_OF_RUNES);

    public static final int NO_RUNE = RunestonesHelper.NUMBER_OF_RUNES;

    protected static final VoxelShape NO_RUNE_SHAPE;
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
        this.setDefaultState(this.getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(RUNE, NO_RUNE));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(RUNE) != NO_RUNE) {
            if (state.get(FACING) == Direction.WEST) {
                return WEST_SHAPE;
            } else if (state.get(FACING) == Direction.SOUTH) {
                return SOUTH_SHAPE;
            } else if (state.get(FACING) == Direction.EAST) {
                return EAST_SHAPE;
            } else {
                return NORTH_SHAPE;
            }
        } else {
            return NO_RUNE_SHAPE;
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient)
            return ActionResult.PASS;

        ItemStack held = player.getStackInHand(hand);
        Integer runeValue = state.get(FrameBlock.RUNE);

        if (hand == Hand.MAIN_HAND && held.isEmpty()) {
            if (runeValue != NO_RUNE) {
                if (!player.isCreative())
                    PlayerHelper.addOrDropStack(player, new ItemStack(Runestones.RUNIC_FRAGMENTS.get(runeValue)));

                world.setBlockState(pos, RunePortals.FRAME_BLOCK.getDefaultState(), 3);
            }
            return ActionResult.CONSUME;
        }

        if (held.getItem() instanceof RunicFragmentItem) {
            RunicFragmentItem fragment = (RunicFragmentItem)held.getItem();
            Direction side = hit.getSide();
            if (side == Direction.UP || side == Direction.DOWN)
                return ActionResult.PASS;

            // if there's already a rune in the frame
            if (runeValue != NO_RUNE) {
                if (!player.isCreative())
                    PlayerHelper.addOrDropStack(player, new ItemStack(Runestones.RUNIC_FRAGMENTS.get(runeValue)));

                world.setBlockState(pos, RunePortals.FRAME_BLOCK.getDefaultState(), 3);
                return ActionResult.CONSUME;
            }

            BlockPos hitPos = hit.getBlockPos();
            BlockState hitState = RunePortals.FRAME_BLOCK.getDefaultState()
                .with(FrameBlock.FACING, side)
                .with(FrameBlock.RUNE, fragment.getRuneValue());

            world.setBlockState(hitPos, hitState, 3);
            world.playSound(null, hitPos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 0.8F, 1.0F);

            if (!player.getAbilities().creativeMode)
                held.decrement(1);

            tryActivate((ServerWorld)world, pos, state);
            return ActionResult.CONSUME;
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
            .with(FACING, rotation.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    public boolean tryActivate(ServerWorld world, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof FrameBlock))
            return false;

        Axis axis = null;
        List<Integer> order = new ArrayList<>();

        if (world.getBlockState(pos.east()).getBlock() instanceof FrameBlock
            || world.getBlockState(pos.west()).getBlock() instanceof FrameBlock) {
            axis = Axis.X;
        } else if (world.getBlockState(pos.north()).getBlock() instanceof FrameBlock
            || world.getBlockState(pos.south()).getBlock() instanceof FrameBlock) {
            axis = Axis.Z;
        } else {

            // try and work out axis from row above/below
            for (int i = -3; i < 4; i++) {
                if (world.getBlockState(pos.east().up(i)).getBlock() instanceof FrameBlock
                    || world.getBlockState(pos.west().up(i)).getBlock() instanceof FrameBlock) {
                    axis = Axis.X;
                    break;
                } else if (world.getBlockState(pos.north().up(i)).getBlock() instanceof FrameBlock
                    || world.getBlockState(pos.south().up(i)).getBlock() instanceof FrameBlock) {
                    axis = Axis.Z;
                    break;
                }
            }
        }

        if (axis == null)
            return false;

        List<Block> validAir = Arrays.asList(
            Blocks.AIR,
            Blocks.CAVE_AIR,
            Blocks.VOID_AIR,
            RunePortals.RUNE_PORTAL_BLOCK
        );

        // try and determine middle of bottom row
        BlockPos start = null;
        if (axis == Axis.X) {
            Direction[] directions = new Direction[]{Direction.EAST, Direction.WEST};
            for (Direction d : directions) {
                for (int y = -3; y <= 3; y++) {
                    for (int x = -3; x <= 3; x++) {
                        BlockPos p = pos.up(y).offset(d, x);
                        if (world.getBlockState(p.up(1)).getBlock() instanceof FrameBlock
                            && validAir.contains(world.getBlockState(p).getBlock())
                            && validAir.contains(world.getBlockState(p.down(1)).getBlock())
                            && validAir.contains(world.getBlockState(p.down(2)).getBlock())
                            && validAir.contains(world.getBlockState(p.down(2).west()).getBlock())
                            && validAir.contains(world.getBlockState(p.down(2).east()).getBlock())
                            && world.getBlockState(p.down(3)).getBlock() instanceof FrameBlock
                        ) {
                            start = p.down(3);
                            break;
                        }
                    }
                }
            }
        } else {
            Direction[] directions = new Direction[]{Direction.NORTH, Direction.SOUTH};
            for (Direction d : directions) {
                for (int y = -3; y <= 3; y++) {
                    for (int z = -3; z <= 3; z++) {
                        BlockPos p = pos.up(y).offset(d, z);
                        if (world.getBlockState(p.up(1)).getBlock() instanceof FrameBlock
                            && validAir.contains(world.getBlockState(p).getBlock())
                            && validAir.contains(world.getBlockState(p.down(1)).getBlock())
                            && validAir.contains(world.getBlockState(p.down(2)).getBlock())
                            && validAir.contains(world.getBlockState(p.down(2).north()).getBlock())
                            && validAir.contains(world.getBlockState(p.down(2).south()).getBlock())
                            && world.getBlockState(p.down(3)).getBlock() instanceof FrameBlock
                        ) {
                            start = p.down(3);
                            break;
                        }
                    }
                }
            }
        }

        if (start == null) {
            return false;
        }

        switch (axis) {
            case X:
                final BlockState eastState = world.getBlockState(start.east(2).up(1));
                final BlockState westState = world.getBlockState(start.west(2).up(1));

                if (!(eastState.getBlock() instanceof FrameBlock) || !(westState.getBlock() instanceof FrameBlock))
                    return false;

                if (eastState.get(FrameBlock.FACING) == Direction.NORTH) {
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.east(2).up(i + 1), order, Direction.NORTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.east(1 - i).up(4), order, Direction.NORTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.west(2).up(3 - i), order, Direction.NORTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.west(1 - i), order, Direction.NORTH)) return false;
                    }
                } else if (westState.get(FrameBlock.FACING) == Direction.SOUTH) {
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.west(2).up(i + 1), order, Direction.SOUTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.west(1 - i).up(4), order, Direction.SOUTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.east(2).up(3 - i), order, Direction.SOUTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.east(1 - i), order, Direction.SOUTH)) return false;
                    }
                }

                break;

            case Z:
                final BlockState northState = world.getBlockState(start.north(2).up(1));
                final BlockState southState = world.getBlockState(start.south(2).up(1));

                if (!(northState.getBlock() instanceof FrameBlock) || !(southState.getBlock() instanceof FrameBlock))
                    return false;

                if (northState.get(FrameBlock.FACING) == Direction.WEST) {
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.north(2).up(i + 1), order, Direction.WEST)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.north(1 - i).up(4), order, Direction.WEST)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.south(2).up(3 - i), order, Direction.WEST)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.south(1 - i), order, Direction.WEST)) return false;
                    }
                } else if (southState.get(FrameBlock.FACING) == Direction.EAST) {
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.south(2).up(i + 1), order, Direction.EAST)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.south(1 - i).up(4), order, Direction.EAST)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.north(2).up(3 - i), order, Direction.EAST)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.north(1 - i), order, Direction.EAST)) return false;
                    }
                }

                break;

            default:
                return false;
        }

        if (order.size() == 12) {
            Charm.LOG.debug("Rune order: " + order);

            Optional<RunePortalManager> optional = RunePortals.getManager(world);
            if (optional.isPresent()) {
                RunePortalManager manager = optional.get();
                manager.createPortal(order, start, axis);
                return true;
            }
        }

        return false;
    }

    private boolean addOrder(ServerWorld world, BlockPos pos, List<Integer> order, Direction expectedFacing) {
        final BlockState s = world.getBlockState(pos);
        if (!(s.getBlock() instanceof FrameBlock))
            return false;

        if (s.get(FrameBlock.RUNE) == NO_RUNE)
            return false;

        if (s.get(FrameBlock.FACING) != expectedFacing)
            return false;

        order.add(s.get(FrameBlock.RUNE));
        return true;
    }

    static {
        NO_RUNE_SHAPE = Block.createCuboidShape(0.0F, 0.0F, 0.0F, 16.0F, 16.0D, 16.0D);

        EAST_FRAME_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 15.0D, 16.0D, 16.0D);
        EAST_RUNE_SHAPE = Block.createCuboidShape(15.0D, 2.0D, 2.0D, 16.0D, 14.0D, 14.0D);

        WEST_FRAME_SHAPE = Block.createCuboidShape(1.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        WEST_RUNE_SHAPE = Block.createCuboidShape(0.0D, 2.0D, 2.0D, 1.0D, 14.0D, 14.0D);

        NORTH_FRAME_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 1.0D, 16.0D, 16.0D, 16.0D);
        NORTH_RUNE_SHAPE = Block.createCuboidShape(2.0D, 2.0D, 0.0D, 14.0D, 14.0D, 1.0D);

        SOUTH_FRAME_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 15.0D);
        SOUTH_RUNE_SHAPE = Block.createCuboidShape(2.0D, 2.0D, 15.0D, 14.0D, 14.0D, 16.0D);

        EAST_SHAPE = VoxelShapes.union(EAST_FRAME_SHAPE, EAST_RUNE_SHAPE);
        WEST_SHAPE = VoxelShapes.union(WEST_FRAME_SHAPE, WEST_RUNE_SHAPE);
        NORTH_SHAPE = VoxelShapes.union(NORTH_FRAME_SHAPE, NORTH_RUNE_SHAPE);
        SOUTH_SHAPE = VoxelShapes.union(SOUTH_FRAME_SHAPE, SOUTH_RUNE_SHAPE);
    }
}
