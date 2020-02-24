package svenhjol.strange.ruins.module;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ObjectHolder;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.ruins.block.EntitySpawnerBlock;
import svenhjol.strange.ruins.tile.EntitySpawnerTileEntity;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUINS, configureEnabled = false)
public class EntitySpawner extends MesonModule
{
    @ObjectHolder("strange:entity_spawner")
    public static TileEntityType<EntitySpawnerTileEntity> tile;

    public static EntitySpawnerBlock block;

    @Override
    public boolean isEnabled()
    {
        return super.isEnabled() && Meson.isModuleEnabled("strange:underground_ruins");
    }

    @Override
    public void init()
    {
        block = new EntitySpawnerBlock(this);
        ResourceLocation res = new ResourceLocation(Strange.MOD_ID, "entity_spawner");
        tile = TileEntityType.Builder.create(EntitySpawnerTileEntity::new, block).build(null);
        RegistryHandler.registerTile(tile, res);
    }
}
