package svenhjol.strange.base.compat;

import net.minecraft.world.gen.feature.structure.Structure;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.world.module.BigDungeonModule;

public class QuarkBigDungeons
{
    public boolean hasModule()
    {
        return ModuleLoader.INSTANCE.isModuleEnabled(BigDungeonModule.class);
    }

    public String getResName()
    {
        if (BigDungeonModule.structure != null) {
            return BigDungeonModule.structure.getStructureName();
        } else {
            return "quark:big_dungeon";
        }
    }

    public Structure<?> getStructure()
    {
        return BigDungeonModule.structure;
    }
}
