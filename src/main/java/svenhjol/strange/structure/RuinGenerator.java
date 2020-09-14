package svenhjol.strange.structure;

import net.minecraft.structure.pool.StructurePool;
import svenhjol.strange.ruin.BambiMountainsRuin;

import java.util.ArrayList;
import java.util.List;

public class RuinGenerator {
    public static List<StructurePool> pools = new ArrayList<>();

    public static void init() {
        BambiMountainsRuin.init();
        pools.add(BambiMountainsRuin.STARTS);
    }
}
