package svenhjol.strange.module.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm.block.CharmBlockWithEntity;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.runestones.enums.IRunestoneMaterial;

import javax.annotation.Nullable;

public class RunestoneBlock extends CharmBlockWithEntity {
    public RunestoneBlock(CharmModule module, IRunestoneMaterial material) {
        super(module, material.getSerializedName() + "_runestone", material.getProperties());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RunestoneBlockEntity(pos, state);
    }
}
