package svenhjol.strange.ruins.tile;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import svenhjol.strange.scrolls.module.EntitySpawner;

import java.util.List;

public class EntitySpawnerTileEntity extends TileEntity implements ITickableTileEntity
{
    private final static String ENTITY = "entity";
    private final static String PERSIST = "persist";
    private final static String HEALTH = "health";
    private final static String META = "meta";

    public ResourceLocation entity;
    public boolean persist;
    public double health;
    public String meta;

    public EntitySpawnerTileEntity()
    {
        super(EntitySpawner.tile);
    }

    @Override
    public void read(CompoundNBT tag)
    {
        super.read(tag);
        this.entity = ResourceLocation.tryCreate(tag.getString(ENTITY));
        this.persist = tag.getBoolean(PERSIST);
        this.health = tag.getDouble(HEALTH);
        this.meta = tag.getString(META);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag)
    {
        super.write(tag);
        tag.putString(ENTITY, entity.toString());
        tag.putBoolean(PERSIST, persist);
        tag.putDouble(HEALTH, health);
        tag.putString(META, meta);
        return tag;
    }

    @Override
    public void tick()
    {
        if (world != null && world.getDifficulty() == Difficulty.PEACEFUL) return;

        BlockPos pos = getPos();
        List<PlayerEntity> players = world.getEntitiesWithinAABB(PlayerEntity.class, new AxisAlignedBB(pos).grow(2.0D));
        if (players.size() == 0) return;

        // remove the spawner, create the entity
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
        trySpawn(pos);
    }

    public void trySpawn(BlockPos pos)
    {

    }
}
