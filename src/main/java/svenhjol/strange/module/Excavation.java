package svenhjol.strange.module;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.block.AncientRubbleBlock;
import svenhjol.strange.blockentity.AncientRubbleBlockEntity;
import svenhjol.strange.client.ExcavationClient;

@Module(mod = Strange.MOD_ID)
public class Excavation extends CharmModule {
    public static final Identifier ID = new Identifier(Strange.MOD_ID, "ancient_rubble");
    public static AncientRubbleBlock ANCIENT_RUBBLE;
    public static BlockEntityType<AncientRubbleBlockEntity> BLOCK_ENTITY;

    public ExcavationClient client;

    @Override
    public void register() {
        ANCIENT_RUBBLE = new AncientRubbleBlock(this);

        BLOCK_ENTITY = BlockEntityType.Builder.create(AncientRubbleBlockEntity::new, ANCIENT_RUBBLE).build(null);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, ID, BLOCK_ENTITY);
    }

    @Override
    public void clientRegister() {
        client = new ExcavationClient(this);
        BlockRenderLayerMap.INSTANCE.putBlock(ANCIENT_RUBBLE, RenderLayer.getCutout());
    }
}
