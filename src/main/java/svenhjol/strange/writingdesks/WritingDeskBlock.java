package svenhjol.strange.writingdesks;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.block.CharmBlock;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.strange.runestones.RunestoneHelper;

import javax.annotation.Nullable;
import java.util.Random;

public class WritingDeskBlock extends CharmBlock {
    public static final IntProperty VARIANT = IntProperty.of("variant", 0, 3);
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final VoxelShape TOP;
    public static final VoxelShape LEGS;
    public static final VoxelShape COLLISION_SHAPE;
    public static final VoxelShape OUTLINE_SHAPE;
    public static final Text TITLE = new TranslatableText("container.strange.writing_desk");

    public WritingDeskBlock(CharmModule module) {
        super(module, WritingDesks.BLOCK_ID.getPath(), AbstractBlock.Settings.copy(Blocks.CARTOGRAPHY_TABLE));
        this.setDefaultState(getDefaultState().with(VARIANT, 0));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient || !ModuleHandler.enabled("strange:runic_tablets")) {
            return ActionResult.SUCCESS;
        } else {
            // ensure client has the latest rune discoveries
            RunestoneHelper.syncLearnedRunesToClient((ServerPlayerEntity)player);
            player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            return ActionResult.CONSUME;
        }
    }

    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity)
            -> new WritingDeskScreenHandler(i, playerInventory, ScreenHandlerContext.create(world, pos)), TITLE);
    }

    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLISION_SHAPE;
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
            .with(FACING, ctx.getPlayerFacing().getOpposite())
            .with(VARIANT, new Random().nextInt(4));
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public ItemGroup getItemGroup() {
        return ItemGroup.DECORATIONS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, VARIANT);
    }

    static {
        TOP = Block.createCuboidShape(0.0D, 13.0D, 0.0D, 16.0D, 13.0D, 16.0D);
        LEGS = Block.createCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 12.0D, 15.0D);
        COLLISION_SHAPE = VoxelShapes.union(TOP, LEGS);
        OUTLINE_SHAPE = VoxelShapes.union(TOP, LEGS);
    }
}
