package svenhjol.strange.runestones.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import svenhjol.meson.MesonModule;

import javax.annotation.Nullable;

public class ObeliskBlock extends BaseRunestoneBlock {
    public static final BooleanProperty DYED = BooleanProperty.create("dyed");
    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

    public ObeliskBlock(MesonModule module, int runeValue) {
        super(module, "obelisk", runeValue, Properties.from(Blocks.STONE));

        this.setDefaultState(this.getStateContainer().getBaseState()
            .with(DYED, false)
            .with(COLOR, DyeColor.WHITE)
        );
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos) {
        return 1;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DYED, COLOR);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        final ItemStack held = player.getHeldItem(handIn);

        if (!worldIn.isRemote
            && !held.isEmpty()
        ) {
            final Item item = held.getItem();
            if (item instanceof DyeItem) {
                final DyeItem dye = (DyeItem)item;
                final DyeColor dyeColor = dye.getDyeColor();

                final BlockState newState = state
                    .with(DYED, true)
                    .with(COLOR, dyeColor);

                worldIn.setBlockState(pos, newState, 2);
                worldIn.playSound(null, pos, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.PLAYERS, 1.0F, 1.0F);

                if (!player.isCreative()) {
                    held.shrink(1);
                }
            }
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.FAIL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState()
            .with(DYED, false)
            .with(COLOR, DyeColor.WHITE);
    }
}
