package svenhjol.strange.base.module;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ObjectHolder;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.tile.EntitySpawnerTileEntity;
import svenhjol.strange.base.block.EntitySpawnerBlock;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUINS)
public class EntitySpawner extends MesonModule
{
    @ObjectHolder("strange:entity_spawner")
    public static TileEntityType<EntitySpawnerTileEntity> tile;

    public static EntitySpawnerBlock block;

    @Override
    public void init()
    {
        block = new EntitySpawnerBlock(this);
        ResourceLocation res = new ResourceLocation(Strange.MOD_ID, "entity_spawner");
        tile = TileEntityType.Builder.create(EntitySpawnerTileEntity::new, block).build(null);
        RegistryHandler.registerTile(tile, res);
    }
}
