package svenhjol.strange.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.sound.BlockSoundGroup;
import svenhjol.meson.MesonModule;
import svenhjol.meson.block.MesonBlock;

public class AmethystBlock extends MesonBlock {
    public AmethystBlock(MesonModule module) {
        super(module, "amethyst", AbstractBlock.Settings
            .copy(Blocks.COBBLESTONE)
            .sounds(BlockSoundGroup.BASALT)
        );
    }
}
