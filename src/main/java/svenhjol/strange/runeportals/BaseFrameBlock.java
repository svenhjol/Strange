package svenhjol.strange.runeportals;

import net.minecraft.block.Block;
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
        ItemStack held = player.getStackInHand(hand);
        BlockPos hitPos = hit.getBlockPos();
        Block block = world.getBlockState(pos).getBlock();
        boolean isFrameBlock = block.equals(RunePortals.FRAME_BLOCK);

        if (isFrameBlock && hand == Hand.MAIN_HAND && held.isEmpty()) {
            int runeValue = state.get(FrameBlock.RUNE);

            if (!world.isClient) {
                if (!player.isCreative())
                    PlayerHelper.addOrDropStack(player, new ItemStack(Runestones.RUNIC_FRAGMENTS.get(runeValue)));
            }

            world.setBlockState(hitPos, RunePortals.RAW_FRAME_BLOCK.getDefaultState(), 3);
            return ActionResult.success(world.isClient);
        }

        if (held.getItem() instanceof RunicFragmentItem) {
            Direction side = hit.getSide();
            if (side == Direction.UP || side == Direction.DOWN)
                return ActionResult.PASS;

            // if there's already a rune in the frame
            if (isFrameBlock) {
                int runeValue = state.get(FrameBlock.RUNE);

                if (!world.isClient && !player.isCreative())
                    PlayerHelper.addOrDropStack(player, new ItemStack(Runestones.RUNIC_FRAGMENTS.get(runeValue)));
//                world.setBlockState(hitPos, RunePortals.RAW_FRAME_BLOCK.getDefaultState(), 18);
            }

            RunicFragmentItem fragment = (RunicFragmentItem)held.getItem();
            BlockState newState = RunePortals.FRAME_BLOCK.getDefaultState()
                .with(FrameBlock.FACING, side)
                .with(FrameBlock.RUNE, fragment.getRuneValue());

            world.setBlockState(pos, newState, 3);
            world.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 0.8F, 1.0F);

            if (!world.isClient) {
                if (!player.getAbilities().creativeMode)
                    held.decrement(1);

                RunePortals.tryActivate((ServerWorld) world, pos, newState);
            }

            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    }
}
