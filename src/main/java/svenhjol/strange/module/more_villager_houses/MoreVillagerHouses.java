package svenhjol.strange.module.more_villager_houses;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.event.SetupStructureCallback;
import svenhjol.strange.init.StrangeDecorations;

import java.util.ArrayList;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID)
public class MoreVillagerHouses extends CharmModule {
    @Config(name = "Beekeeper house weight", description = "Chance of a custom building to spawn. For reference, a vanilla library is 5.")
    public static int buildingWeight = 5;

    @Override
    public void runWhenEnabled() {

        ServerLifecycleEvents.SERVER_STARTING.register(this::handleServerStarting);
    }

    private void handleServerStarting(MinecraftServer server) {
        StructureProcessorList vanillaProcessor = server.registryAccess().registryOrThrow(Registry.PROCESSOR_LIST_REGISTRY).get(new ResourceLocation("minecraft", "mossify_10_percent"));
        List<StructureProcessor> processor = new ArrayList<>(vanillaProcessor.list());
        processor.addAll(StrangeDecorations.SINGLE_POOL_ELEMENT_PROCESSORS);

        Registry<StructureTemplatePool> poolRegistry = server.registryAccess().registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);

        // register beekeeper houses
        SetupStructureCallback.addVillageHouse(SetupStructureCallback.VillageType.PLAINS, new ResourceLocation("strange:village/plains/houses/plains_beejack"), buildingWeight, processor, poolRegistry);
        SetupStructureCallback.addVillageHouse(SetupStructureCallback.VillageType.PLAINS, new ResourceLocation("strange:village/plains/houses/plains_beekeeper_1"), buildingWeight, processor, poolRegistry);
        SetupStructureCallback.addVillageHouse(SetupStructureCallback.VillageType.DESERT, new ResourceLocation("strange:village/desert/houses/desert_beekeeper_1"), buildingWeight, processor, poolRegistry);
        SetupStructureCallback.addVillageHouse(SetupStructureCallback.VillageType.SAVANNA, new ResourceLocation("strange:village/savanna/houses/savanna_beekeeper_1"), buildingWeight, processor, poolRegistry);
        SetupStructureCallback.addVillageHouse(SetupStructureCallback.VillageType.SAVANNA, new ResourceLocation("strange:village/savanna/houses/savanna_beekeeper_2"), buildingWeight, processor, poolRegistry);
        SetupStructureCallback.addVillageHouse(SetupStructureCallback.VillageType.TAIGA, new ResourceLocation("strange:village/taiga/houses/taiga_beekeeper_1"), buildingWeight, processor, poolRegistry);
        SetupStructureCallback.addVillageHouse(SetupStructureCallback.VillageType.SNOWY, new ResourceLocation("strange:village/snowy/houses/snowy_lumberbee_1"), buildingWeight, processor, poolRegistry);
    }
}
