package svenhjol.strange.module.structures.processor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public abstract class BaseOreStructureProcessor extends StructureProcessor {
    protected Random random;
    protected float chance;

    @Nullable
    @Override
    public StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureBlockInfo OMGNO, StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        random = structurePlaceSettings.getRandom(structureBlockInfo2.pos);
        BlockState state = structureBlockInfo2.state;
        BlockPos pos = structureBlockInfo2.pos;
        Block block = state.getBlock();

        if (!isOre(block)) {
            return structureBlockInfo2;
        }

        if (random.nextFloat() < chance) {
            List<BlockState> pool;
            pool = getOreReplacements();

            if (pool.size() > 0) {
                BlockState newState = pool.get(random.nextInt(pool.size()));
                return new StructureBlockInfo(pos, newState, null);
            }
        }

        return new StructureBlockInfo(pos, Blocks.AIR.defaultBlockState(), null);
    }

    protected abstract boolean isOre(Block block);

    protected abstract List<BlockState> getOreReplacements();
}
