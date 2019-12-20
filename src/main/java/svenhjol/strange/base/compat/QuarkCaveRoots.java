package svenhjol.strange.base.compat;

import net.minecraft.block.Block;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.world.module.CaveRootsModule;

import javax.annotation.Nullable;

public class QuarkCaveRoots
{
    public boolean hasModule()
    {
        return ModuleLoader.INSTANCE.isModuleEnabled(CaveRootsModule.class);
    }

    @Nullable
    public Block getRootBlock()
    {
        if (hasModule()) {
            return CaveRootsModule.root;
        }
        return null;
    }
}
