package svenhjol.strange.runeportals;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.block.CharmBlock;
import svenhjol.charm.base.helper.PlayerHelper;
import svenhjol.strange.runestones.Runestones;
import svenhjol.strange.runestones.RunicFragmentItem;

public abstract class BaseFrameBlock extends CharmBlock {
    public BaseFrameBlock(CharmModule module, String name, Settings props, String... loadedMods) {
        super(module, name, props, loadedMods);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient)
            return ActionResult.PASS;

        ItemStack held = player.getStackInHand(hand);
        boolean isFrameBlock = state.getBlock() == RunePortals.FRAME_BLOCK;

        if (isFrameBlock && hand == Hand.MAIN_HAND && held.isEmpty()) {
            int runeValue = state.get(FrameBlock.RUNE);
            if (!player.isCreative())
                PlayerHelper.addOrDropStack(player, new ItemStack(Runestones.RUNIC_FRAGMENTS.get(runeValue)));

            world.setBlockState(pos, RunePortals.RAW_FRAME_BLOCK.getDefaultState(), 3);
            return ActionResult.CONSUME;
        }

        if (held.getItem() instanceof RunicFragmentItem) {
            RunicFragmentItem fragment = (RunicFragmentItem)held.getItem();
            Direction side = hit.getSide();
            if (side == Direction.UP || side == Direction.DOWN)
                return ActionResult.PASS;

            // if there's already a rune in the frame
            if (isFrameBlock) {
                int runeValue = state.get(FrameBlock.RUNE);
                if (!player.isCreative())
                    PlayerHelper.addOrDropStack(player, new ItemStack(Runestones.RUNIC_FRAGMENTS.get(runeValue)));

                world.setBlockState(pos, RunePortals.RAW_FRAME_BLOCK.getDefaultState(), 3);
                return ActionResult.CONSUME;
            }

            BlockPos hitPos = hit.getBlockPos();
            BlockState hitState = RunePortals.FRAME_BLOCK.getDefaultState()
                .with(FrameBlock.FACING, side)
                .with(FrameBlock.RUNE, fragment.getRuneValue());

            world.setBlockState(hitPos, hitState, 3);
            world.playSound(null, hitPos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 0.8F, 1.0F);

            if (!player.getAbilities().creativeMode)
                held.decrement(1);

            RunePortals.tryActivate((ServerWorld)world, pos, hitState);
            return ActionResult.CONSUME;
        }

        return ActionResult.PASS;
    }
}
