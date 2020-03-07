package svenhjol.strange.base.module;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ObjectHolder;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.block.EntitySpawnerBlock;
import svenhjol.strange.base.tile.EntitySpawnerTileEntity;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.BASE, alwaysEnabled = true)
public class EntitySpawner extends MesonModule {
    @Config(name = "Trigger distance", description = "Player will trigger EntitySpawner blocks when closer than this distance.")
    public static int triggerDistance = 16;

    @ObjectHolder("strange:entity_spawner")
    public static TileEntityType<EntitySpawnerTileEntity> tile;

    public static EntitySpawnerBlock block;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void init() {
        block = new EntitySpawnerBlock(this);
        ResourceLocation res = new ResourceLocation(Strange.MOD_ID, "entity_spawner");
        tile = TileEntityType.Builder.create(EntitySpawnerTileEntity::new, block).build(null);
        RegistryHandler.registerTile(tile, res);
    }
}
