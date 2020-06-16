package svenhjol.strange.runestones.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.meson.iface.IMesonBlock;
import svenhjol.strange.Strange;
import svenhjol.strange.runestones.message.ClientRunePortalAction;
import svenhjol.strange.runestones.module.RunePortals;
import svenhjol.strange.runestones.tileentity.RunePortalTileEntity;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class RunePortalBlock extends EndPortalBlock implements IMesonBlock {
    public static final EnumProperty<Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    protected static final VoxelShape X_AABB = Block.makeCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 12.0D);
    protected static final VoxelShape Z_AABB = Block.makeCuboidShape(4.0D, 0.0D, 0.0D, 12.0D, 16.0D, 16.0D);

    public RunePortalBlock(MesonModule module) {
        super(Properties
            .create(Material.PORTAL, MaterialColor.BLACK)
            .doesNotBlockMovement()
            .lightValue(15)
            .hardnessAndResistance(-1.0F, 3600000.0F)
            .noDrops()
        );
        register(module, "rune_portal");
        this.setDefaultState(this.stateContainer.getBaseState().with(AXIS, Axis.X));
    }

    @Override
    public ItemGroup getItemGroup() {
        return ItemGroup.SEARCH;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return new RunePortalTileEntity();
    }

    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        switch(state.get(AXIS)) {
            case Z:
                return Z_AABB;
            case X:
            default:
                return X_AABB;
        }
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        // no op
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isRemote
            && entity instanceof PlayerEntity
            && !entity.isPassenger()
            && !entity.isBeingRidden()
            && entity.isNonBoss()
            && VoxelShapes.compare(VoxelShapes.create(entity.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ())), state.getShape(world, pos), IBooleanFunction.AND)
        ) {
            final RunePortalTileEntity tile = getTileEntity(world, pos);
            if (tile == null) return;
            BlockPos dest = tile.position;
            int dim = tile.dimension;
            tile.markDirty();

            Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendToAll(new ClientRunePortalAction(ClientRunePortalAction.TRAVELLED, pos));

            PlayerEntity player = (PlayerEntity)entity;
            PlayerHelper.changeDimension(player, dim);
            PlayerHelper.teleport(player, dest, dim, p -> {
                int x = Math.max(-30000000, Math.min(30000000, dest.getX()));
                int y = Math.max(-64, Math.min(1024, dest.getY()));
                int z = Math.max(-30000000, Math.min(30000000, dest.getZ()));
                player.setPositionAndUpdate(x, y, z);
                Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendToAll(new ClientRunePortalAction(ClientRunePortalAction.TRAVELLED, dest));
            });
        }
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, IBlockReader world, BlockPos pos, Entity entity) {
        return false;
    }

    @Nullable
    public RunePortalTileEntity getTileEntity(World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof RunePortalTileEntity) {
            return (RunePortalTileEntity) tile;
        }
        return null;
    }

    @Nullable
    public BlockPos getPortal(World world, BlockPos pos) {
        RunePortalTileEntity tile = getTileEntity(world, pos);
        if (tile == null) return null;
        return tile.position;
    }

    @Nullable
    public int getColor(World world, BlockPos pos) {
        RunePortalTileEntity tile = getTileEntity(world, pos);
        if (tile == null) return 0;
        return tile.color;
    }

    public void setPortal(World world, BlockPos pos, BlockPos dest, int dimension, int color, int orientation) {
        RunePortalTileEntity tile = getTileEntity(world, pos);
        if (tile != null) {
            tile.position = dest;
            tile.dimension = dimension;
            tile.color = color;
            tile.orientation = orientation; // needed for TER
            tile.markDirty();

            BlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 2);
        }
    }

    public void remove(World world, BlockPos pos) {
        world.destroyBlock(pos, false);
        RunePortals.breakSurroundingPortals(world, pos);
    }
}
