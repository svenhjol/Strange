package svenhjol.strange.runestones.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.DyeColor;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.ToolType;
import svenhjol.meson.MesonModule;
import svenhjol.meson.block.MesonBlock;

public class MoonstoneBlock extends MesonBlock {
    private DyeColor color;
    private final Vec3d vec;

    public MoonstoneBlock(MesonModule module, DyeColor color) {
        super(module, "moonstone_" + color.getName(),
            Block.Properties.create(Material.GLASS, MaterialColor.BLUE)
                .hardnessAndResistance(0.3F, 0F)
                .sound(SoundType.GLASS)
                .lightValue(11)
                .harvestLevel(0)
                .harvestTool(ToolType.PICKAXE)
            );

        float[] comp = color.getColorComponentValues();
        this.vec = new Vec3d(comp[0], comp[1], comp[2]);
        this.color = color;

    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public float getAmbientOcclusionLightValue(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState adjacent, Direction side) {
        return adjacent.getBlock() instanceof MoonstoneBlock || super.isSideInvisible(state, adjacent, side);
    }

    @Override
    public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public Vec3d getFogColor(BlockState state, IWorldReader world, BlockPos pos, Entity entity, Vec3d originalColor, float partialTicks) {
        return vec;
    }

    @Override
    public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canEntitySpawn(BlockState state, IBlockReader worldIn, BlockPos pos, EntityType<?> type) {
        return false;
    }
}
