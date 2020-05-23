package svenhjol.strange.runestones.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.DyeColor;
import svenhjol.meson.MesonModule;
import svenhjol.meson.block.MesonBlock;

public class MoonstoneBlock extends MesonBlock {
    private DyeColor color;

    public MoonstoneBlock(MesonModule module, DyeColor color) {
        super(module, "moonstone_" + color.getName(),
            Block.Properties.from(Blocks.DIAMOND_BLOCK));

        this.color = color;
    }
}
