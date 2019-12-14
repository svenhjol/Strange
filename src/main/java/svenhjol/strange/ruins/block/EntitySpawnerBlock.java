package svenhjol.strange.ruins.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import svenhjol.meson.MesonModule;
import svenhjol.meson.block.MesonBlock;
import svenhjol.strange.ruins.tile.EntitySpawnerTileEntity;

import javax.annotation.Nullable;

public class EntitySpawnerBlock extends MesonBlock
{
    public EntitySpawnerBlock(MesonModule module)
    {
        super(module, "entity_spawner", Block.Properties
            .create(Material.IRON, MaterialColor.BLACK)
            .hardnessAndResistance(-1.0F, 3600000.0F)
            .noDrops());
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new EntitySpawnerTileEntity();
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }
}
