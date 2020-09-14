package svenhjol.strange.ruin;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.processor.StructureProcessorLists;
import net.minecraft.util.Identifier;
import svenhjol.strange.Strange;

public class BambiMountainsRuin {
    public static StructurePool STARTS;

    public static void init() {

        // starts
        STARTS = StructurePools.register(
            new StructurePool(
                new Identifier(Strange.MOD_ID, "ruins/bambi_mountains/starts"),
                new Identifier("empty"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/start1", StructureProcessorLists.EMPTY), 1)
                ),
                StructurePool.Projection.RIGID
            )
        );

        // rooms
        StructurePools.register(
            new StructurePool(
                new Identifier(Strange.MOD_ID, "ruins/bambi_mountains/rooms"),
                new Identifier(Strange.MOD_ID, "ruins/bambi_mountains/ends"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/room_blacksmith", StructureProcessorLists.EMPTY), 1),
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/room_corner1", StructureProcessorLists.EMPTY), 1),
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/room_corner2", StructureProcessorLists.EMPTY), 1),
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/room_derp1", StructureProcessorLists.EMPTY), 1),
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/room_skeletons", StructureProcessorLists.EMPTY), 1),
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/room_trap", StructureProcessorLists.EMPTY), 1),
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/room_zombies", StructureProcessorLists.EMPTY), 1)
                ),
                StructurePool.Projection.RIGID
            )
        );

        // corridors
        StructurePools.register(
            new StructurePool(
                new Identifier(Strange.MOD_ID, "ruins/bambi_mountains/corridors"),
                new Identifier(Strange.MOD_ID, "ruins/bambi_mountains/ends"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/corridor_hall", StructureProcessorLists.EMPTY), 1),
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/corridor_shattered", StructureProcessorLists.EMPTY), 1),
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/corridor_small", StructureProcessorLists.EMPTY), 1),
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/corridor_spawner", StructureProcessorLists.EMPTY), 1),
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/corridor_tiny", StructureProcessorLists.EMPTY), 1)
                ),
                StructurePool.Projection.RIGID
            )
        );

        // ends
        StructurePools.register(
            new StructurePool(
                new Identifier(Strange.MOD_ID, "ruins/bambi_mountains/ends"),
                new Identifier("empty"),
                ImmutableList.of(
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/end_chest", StructureProcessorLists.EMPTY), 1),
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/end_erosion", StructureProcessorLists.EMPTY), 1),
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/end_potions", StructureProcessorLists.EMPTY), 1),
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/end_wall1", StructureProcessorLists.EMPTY), 1),
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/end_wall2", StructureProcessorLists.EMPTY), 1),
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/end_wall3", StructureProcessorLists.EMPTY), 1),
                    Pair.of(StructurePoolElement.method_30435("strange:ruins/bambi_mountains/end_wall4", StructureProcessorLists.EMPTY), 1)
                ),
                StructurePool.Projection.RIGID
            )
        );

    }
}
