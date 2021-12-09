package svenhjol.strange.event;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.structures.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import svenhjol.charm.enums.ICharmEnum;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface SetupStructureCallback {
    Map<ResourceLocation, StructureTemplatePool> vanillaPools = new HashMap<>();

    Event<SetupStructureCallback> EVENT = EventFactory.createArrayBacked(SetupStructureCallback.class, (listeners) -> () -> {
        for (SetupStructureCallback listener : listeners) {
            listener.interact();
        }
    });

    @Nullable
    static StructureTemplatePool getVanillaPool(ResourceLocation id) {
        if (!vanillaPools.containsKey(id)) {
            StructureTemplatePool pool = BuiltinRegistries.TEMPLATE_POOL.get(id);
            if (pool == null) return null;

            // convert elementCounts to mutable list
            List<Pair<StructurePoolElement, Integer>> elementCounts = pool.rawTemplates;
            pool.rawTemplates = new ArrayList<>(elementCounts);

            //noinspection ConstantConditions
            if (false) { // DELETES ALL IN POOL, DO NOT USE!
                pool.rawTemplates = new ArrayList<>();
            }

            vanillaPools.put(id, pool);
        }

        return vanillaPools.get(id);
    }

    static void addStructurePoolElement(ResourceLocation poolId, ResourceLocation pieceId, StructureProcessorList processor, StructureTemplatePool.Projection projection, int count, Registry<StructureTemplatePool> poolRegistry) {
        Pair<Function<StructureTemplatePool.Projection, LegacySinglePoolElement>, Integer> pair =
            Pair.of(StructurePoolElement.legacy(pieceId.toString(), processor), count);

        StructurePoolElement element = pair.getFirst().apply(projection);
        StructureTemplatePool pool = poolRegistry.get(poolId);
        if (pool == null) return;
        
        // add custom piece to the element counts
        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(element, count));
        pool.rawTemplates = listOfPieceEntries;
        
        // add custom piece to the elements
        for (int i = 0; i < count; i++) {
            pool.templates.add(element);
        }
    }

    static void addVillageHouse(VillageType type, ResourceLocation pieceId, int count, List<StructureProcessor> processor, Registry<StructureTemplatePool> poolRegistry) {
        ResourceLocation houses = new ResourceLocation("village/" + type.getSerializedName() + "/houses");
        StructureTemplatePool.Projection projection = StructureTemplatePool.Projection.RIGID;
        addStructurePoolElement(houses, pieceId, new StructureProcessorList(processor), projection, count, poolRegistry);
    }

    void interact();

    enum VillageType implements ICharmEnum {
        DESERT,
        PLAINS,
        SAVANNA,
        SNOWY,
        TAIGA
    }
}
