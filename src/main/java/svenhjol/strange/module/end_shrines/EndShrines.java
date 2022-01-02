package svenhjol.strange.module.end_shrines;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.dimensions.Dimensions;
import svenhjol.strange.module.dimensions.floating_islands.FloatingIslandsDimension;
import svenhjol.strange.module.dimensions.mirror.MirrorDimension;
import svenhjol.strange.module.end_shrines.processor.EndShrinePortalProcessor;

import java.util.ArrayList;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID)
public class EndShrines extends CharmModule {
    public static final ResourceLocation BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "end_shrine_portal");
    public static EndShrinePortalBlock END_SHRINE_PORTAL_BLOCK;
    public static BlockEntityType<EndShrinePortalBlockEntity> END_SHRINE_PORTAL_BLOCK_ENTITY;
    public static StructureProcessorType<EndShrinePortalProcessor> END_SHRINE_PORTAL_PROCESSOR;

    public static List<ResourceLocation> DESTINATIONS = new ArrayList<>();

    @Override
    public void register() {
        END_SHRINE_PORTAL_BLOCK = new EndShrinePortalBlock(this);
        END_SHRINE_PORTAL_BLOCK_ENTITY = CommonRegistry.blockEntity(BLOCK_ID, EndShrinePortalBlockEntity::new, END_SHRINE_PORTAL_BLOCK);
        END_SHRINE_PORTAL_PROCESSOR = CommonRegistry.structureProcessor(new ResourceLocation(Strange.MOD_ID, "end_shrine_portal"), () -> EndShrinePortalProcessor.CODEC);
    }

    @Override
    public void runWhenEnabled() {
        DESTINATIONS.add(Level.OVERWORLD.location());

        if (Dimensions.mirrorEnabled()) {
            DESTINATIONS.add(MirrorDimension.ID);
        }

        if (Dimensions.floatingIslandsEnabled()) {
            DESTINATIONS.add(FloatingIslandsDimension.ID);
        }
    }
}
