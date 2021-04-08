package svenhjol.strange.runeportals;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.block.CharmBlockWithEntity;

public class RunePortalBlock extends CharmBlockWithEntity {
    public static final EnumProperty<Axis> AXIS = Properties.HORIZONTAL_AXIS;

    protected static final VoxelShape FACING_X = Block.createCuboidShape(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape FACING_Z = Block.createCuboidShape(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    public RunePortalBlock(CharmModule module) {
        super(module, "rune_portal", FabricBlockSettings.of(Material.PORTAL)
            .sounds(BlockSoundGroup.GLASS)
            .noCollision()
            .strength(-1.0F)
            .luminance(11)
            .dropsNothing());

        this.setDefaultState(this.getDefaultState().with(AXIS, Axis.X));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch(state.get(AXIS)) {
            case Z:
                return FACING_Z;
            case X:
            default:
                return FACING_X;
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(AXIS);
    }

    @Override
    public void addStacksForDisplay(ItemGroup group, DefaultedList<ItemStack> list) {
        // nope
    }

    @Override
    public ItemGroup getItemGroup() {
        return ItemGroup.SEARCH;
    }

    @Override
    public boolean enabled() {
        return module.enabled;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RunePortalBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        Direction.Axis axis = direction.getAxis();
        Direction.Axis axis2 = state.get(AXIS);
        boolean bl = axis2 != axis && axis.isHorizontal();
        return !bl && (!neighborState.isOf(this) || !neighborState.isOf(RunePortals.FRAME_BLOCK))
            ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    public void remove(World world, BlockPos pos) {
        world.removeBlock(pos, false);
        RunePortals.breakSurroundingPortals(world, pos);
    }
}
