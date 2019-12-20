package svenhjol.strange.base.compat;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.meson.enums.WoodType;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.building.module.VariantChestsModule;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class QuarkVariantChests
{
    public boolean hasModule()
    {
        return ModuleLoader.INSTANCE.isModuleEnabled(VariantChestsModule.class);
    }

    @Nullable
    public Block getRandomChest(Random rand)
    {
        List<WoodType> types = Arrays.asList(WoodType.values());
        WoodType type = types.get(rand.nextInt(types.size()));
        ResourceLocation res = new ResourceLocation(Quark.MOD_ID, type.name().toLowerCase() + "_chest");
        return ForgeRegistries.BLOCKS.getValue(res);
    }
}
