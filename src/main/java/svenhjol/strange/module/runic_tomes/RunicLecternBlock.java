package svenhjol.strange.module.runic_tomes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.block.CharmBlockWithEntity;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.init.StrangeParticles;
import svenhjol.strange.module.journals.JournalHelper;
import svenhjol.strange.module.journals.Journals;

import java.util.Random;

public class RunicLecternBlock extends CharmBlockWithEntity {
    public static final DirectionProperty FACING;
    public static final VoxelShape SHAPE_BASE;
    public static final VoxelShape SHAPE_POST;
    public static final VoxelShape SHAPE_COMMON;
    public static final VoxelShape SHAPE_TOP_PLATE;
    public static final VoxelShape SHAPE_COLLISION;
    public static final VoxelShape SHAPE_WEST;
    public static final VoxelShape SHAPE_NORTH;
    public static final VoxelShape SHAPE_EAST;
    public static final VoxelShape SHAPE_SOUTH;

    protected RunicLecternBlock(CharmModule module) {
        super(module, "runic_lectern", BlockBehaviour.Properties.copy(Blocks.LECTERN));
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!level.isClientSide) {
            boolean result = tryReadTome((ServerLevel) level, pos, (ServerPlayer) player);
            if (!result) {
                return InteractionResult.CONSUME;
            }
            player.openMenu(state.getMenuProvider(level, pos));
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState state2, boolean bl) {
        RunicLecternBlockEntity runicLectern = getBlockEntity(level, pos);
        if (runicLectern != null) {
            ItemStack tome = runicLectern.getTome();
            ItemStack sacrifice = runicLectern.getItem(0);
            Direction facing = state.getValue(FACING);

            popItem(level, pos, tome, facing);
            runicLectern.clearTome();

            if (!sacrifice.isEmpty()) {
                popItem(level, pos, sacrifice, facing);
                runicLectern.clearContent();
            }

            level.removeBlockEntity(pos);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return SHAPE_COMMON;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE_COLLISION;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        switch(state.getValue(FACING)) {
            case NORTH:
                return SHAPE_NORTH;
            case SOUTH:
                return SHAPE_SOUTH;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
            default:
                return SHAPE_COMMON;
        }
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RunicLecternBlockEntity(pos, state);
    }

    @Nullable
    public RunicLecternBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof RunicLecternBlockEntity) {
            return (RunicLecternBlockEntity) blockEntity;
        }
        return null;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> list) {
        // no
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    protected boolean tryReadTome(ServerLevel level, BlockPos pos, ServerPlayer player) {
        RunicLecternBlockEntity lectern = getBlockEntity(level, pos);
        if (lectern == null) return false;

        // Prevent spam activation or cheesing the tome from the lectern.
        // ActivatedTicks is set to the current gametime when a tome is activated.
        long activatedTicks = lectern.getActivatedTicks();
        long gameTime = level.getGameTime();
        if (activatedTicks > 0 && gameTime - activatedTicks < 100) {
            return false;
        }

        // Convert the runic lectern back to a vanilla lectern if there's no tome.
        if (!lectern.hasTome()) {
            BlockState currentState = level.getBlockState(pos);
            BlockState newState = Blocks.LECTERN.defaultBlockState();

            newState = newState.setValue(LecternBlock.FACING, currentState.getValue(RunicLecternBlock.FACING));
            level.removeBlockEntity(pos);
            level.setBlockAndUpdate(pos, newState);
            return false;
        }

        CompoundTag tomeTag = new CompoundTag();
        lectern.getTome().save(tomeTag);

        // try and learn this if not already known
        Journals.getJournalData(player).ifPresent(journal -> {
            String runes = RunicTomeItem.getRunes(lectern.getTome());
            if (!runes.isEmpty()) {
                JournalHelper.tryLearnPhrase(runes, journal);
                Journals.sendSyncJournal(player);
            }
        });

        NetworkHelper.sendPacketToClient(player, RunicTomes.MSG_CLIENT_SET_LECTERN_TOME, buf -> buf.writeNbt(tomeTag));
        return true;
    }

    protected void popItem(Level level, BlockPos pos, ItemStack stack, Direction facing) {
        float fx = 0.25F * facing.getStepX();
        float fz = 0.25F * facing.getStepZ();

        ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5 + fx, pos.getY() + 1, pos.getZ() + 0.5 + fz, stack);
        entity.setDefaultPickUpDelay();
        level.addFreshEntity(entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        level.addParticle(StrangeParticles.ILLAGERALT, pos.getX() + 0.5, pos.getY() + 1.15, pos.getZ() + 0.5, random.nextFloat() - 0.5F, random.nextFloat() + 0.05f, random.nextFloat() - 0.5F);
    }

    static {
        FACING = HorizontalDirectionalBlock.FACING;
        SHAPE_BASE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
        SHAPE_POST = Block.box(4.0D, 2.0D, 4.0D, 12.0D, 14.0D, 12.0D);
        SHAPE_COMMON = Shapes.or(SHAPE_BASE, SHAPE_POST);
        SHAPE_TOP_PLATE = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 15.0D, 16.0D);
        SHAPE_COLLISION = Shapes.or(SHAPE_COMMON, SHAPE_TOP_PLATE);
        SHAPE_WEST = Shapes.or(Block.box(1.0D, 10.0D, 0.0D, 5.333333D, 14.0D, 16.0D), Block.box(5.333333D, 12.0D, 0.0D, 9.666667D, 16.0D, 16.0D), Block.box(9.666667D, 14.0D, 0.0D, 14.0D, 18.0D, 16.0D), SHAPE_COMMON);
        SHAPE_NORTH = Shapes.or(Block.box(0.0D, 10.0D, 1.0D, 16.0D, 14.0D, 5.333333D), Block.box(0.0D, 12.0D, 5.333333D, 16.0D, 16.0D, 9.666667D), Block.box(0.0D, 14.0D, 9.666667D, 16.0D, 18.0D, 14.0D), SHAPE_COMMON);
        SHAPE_EAST = Shapes.or(Block.box(10.666667D, 10.0D, 0.0D, 15.0D, 14.0D, 16.0D), Block.box(6.333333D, 12.0D, 0.0D, 10.666667D, 16.0D, 16.0D), Block.box(2.0D, 14.0D, 0.0D, 6.333333D, 18.0D, 16.0D), SHAPE_COMMON);
        SHAPE_SOUTH = Shapes.or(Block.box(0.0D, 10.0D, 10.666667D, 16.0D, 14.0D, 15.0D), Block.box(0.0D, 12.0D, 6.333333D, 16.0D, 16.0D, 10.666667D), Block.box(0.0D, 14.0D, 2.0D, 16.0D, 18.0D, 6.333333D), SHAPE_COMMON);
    }
}
