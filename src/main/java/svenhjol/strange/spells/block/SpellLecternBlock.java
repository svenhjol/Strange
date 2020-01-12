package svenhjol.strange.spells.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.meson.iface.IMesonBlock;
import svenhjol.strange.spells.tile.SpellLecternTileEntity;

import javax.annotation.Nullable;
import java.util.Random;

public class SpellLecternBlock extends LecternBlock implements IMesonBlock
{
    public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 15);

    public SpellLecternBlock()
    {
        super(Block.Properties.from(Blocks.LECTERN));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        super.fillStateContainer(builder);
        builder.add(COLOR);
    }

    @Override
    public ItemGroup getItemGroup()
    {
        return null;
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new SpellLecternTileEntity();
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn)
    {
        return new SpellLecternTileEntity();
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (!(tile instanceof SpellLecternTileEntity)) return;

        SpellLecternTileEntity lectern = (SpellLecternTileEntity)tile;
        ItemStack book = lectern.getBook();
        if (book == null) return;

        worldIn.addEntity(new ItemEntity(worldIn, pos.getX(), pos.getY() + 0.5D, pos.getZ(), book));
    }

    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        super.animateTick(stateIn, worldIn, pos, rand);
        if (rand.nextInt(7) == 0) {
            worldIn.addParticle(ParticleTypes.ENCHANT, pos.getX() + rand.nextFloat(), pos.getY() + 2.0F, pos.getZ() + rand.nextFloat(), 0, 0, 0);
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (!(tile instanceof SpellLecternTileEntity)) return false;

        SpellLecternTileEntity lectern = (SpellLecternTileEntity)tile;
        ItemStack book = lectern.getBook();
        if (book == null) return false;

        PlayerHelper.addOrDropStack(player, book);

        restoreLectern(worldIn, pos, state);
        return true;
    }

    @Override
    public boolean canProvidePower(BlockState state)
    {
        return false;
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
    {
        return 0;
    }

    @Override
    public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
    {
        return 0;
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state)
    {
        return false;
    }

    public static void restoreLectern(World world, BlockPos pos, BlockState state)
    {
        world.removeTileEntity(pos);
        BlockState replace = Blocks.LECTERN.getDefaultState();
        replace = replace.with(LecternBlock.FACING, state.get(SpellLecternBlock.FACING));
        world.setBlockState(pos, replace);
    }
}
