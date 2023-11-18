package svenhjol.strange.feature.cooking_pots;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import svenhjol.charmony.base.CharmonyBlockWithEntity;
import svenhjol.charmony.common.CommonFeature;

public class CookingPotBlock extends CharmonyBlockWithEntity {
    static IntegerProperty PORTIONS = IntegerProperty.create("portions", 0, CookingPots.getNumberOfPortions() - 1);
    static BooleanProperty HAS_FIRE = BooleanProperty.create("has_fire");

    static final VoxelShape RAY_TRACE_SHAPE;
    static final VoxelShape OUTLINE_SHAPE;

    public CookingPotBlock(CommonFeature feature) {
        super(feature, Properties.of()
            .requiresCorrectToolForDrops()
            .strength(2.0f)
            .noOcclusion()
            .sound(SoundType.COPPER));

        registerDefaultState(defaultBlockState()
            .setValue(HAS_FIRE, false)
            .setValue(PORTIONS, 0));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CookingPotBlockEntity(pos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return RAY_TRACE_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PORTIONS, HAS_FIRE);
    }

    static {
        RAY_TRACE_SHAPE = box(2.0d, 4.0d, 2.0d, 14.0d, 16.0d, 14.0d);
        OUTLINE_SHAPE = Shapes.join(Shapes.block(),
            Shapes.or(
                box(0.0d, 0.0d, 4.0d, 16.0d, 3.0d, 12.0d),
                box(4.0d, 0.0d, 0.0d, 12.0d, 3.0d, 16.0d),
                box(2.0d, 0.0d, 2.0d, 14.0d, 3.0d, 14.0d), RAY_TRACE_SHAPE),
            BooleanOp.ONLY_FIRST);
    }
}
