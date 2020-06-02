package svenhjol.strange.runestones.block;

import net.minecraft.block.SlabBlock;
import net.minecraft.item.ItemGroup;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.IMesonBlock;
import svenhjol.strange.runestones.module.Amethyst;

public class PolishedAmethystSlabBlock extends SlabBlock implements IMesonBlock {
    public PolishedAmethystSlabBlock(MesonModule module) {
        super(Properties.from(Amethyst.block));
        register(module, "polished_amethyst_slab");
    }

    @Override
    public ItemGroup getItemGroup() {
        return ItemGroup.BUILDING_BLOCKS;
    }

    @Override
    public boolean isEnabled() {
        return Meson.isModuleEnabled("strange:amethyst");
    }
}
