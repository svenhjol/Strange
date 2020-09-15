package svenhjol.strange.module;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.meson.mixin.accessor.RenderLayersAccessor;
import svenhjol.strange.Strange;
import svenhjol.strange.block.EntitySpawnerBlock;
import svenhjol.strange.blockentity.EntitySpawnerBlockEntity;

@Module(description = "Spawns entities when a player is within range.", alwaysEnabled = true)
public class EntitySpawner extends MesonModule {
    public static final Identifier ID = new Identifier(Strange.MOD_ID, "entity_spawner");
    public static EntitySpawnerBlock ENTITY_SPAWNER;
    public static BlockEntityType<EntitySpawnerBlockEntity> BLOCK_ENTITY;

    @Config(name = "Trigger distance", description = "Player will trigger EntitySpawner blocks when closer than this distance.")
    public static int triggerDistance = 16;

    @Override
    public void register() {
        ENTITY_SPAWNER = new EntitySpawnerBlock(this);
        BLOCK_ENTITY = BlockEntityType.Builder.create(EntitySpawnerBlockEntity::new, ENTITY_SPAWNER).build(null);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, ID, BLOCK_ENTITY);
    }

    @Override
    public void clientRegister() {
        RenderLayersAccessor.getBlocks().put(ENTITY_SPAWNER, RenderLayer.getCutout());
    }
}
