package svenhjol.strange.runeportals;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
import svenhjol.strange.runestones.RunicFragmentItem;

public abstract class BaseFrameBlock extends CharmBlock {
    public BaseFrameBlock(CharmModule module, String name, Settings props) {
        super(module, name, props);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack held = player.getStackInHand(hand);

        if (held.getItem() instanceof RunicFragmentItem) {
            RunicFragmentItem fragment = (RunicFragmentItem)held.getItem();
            Direction side = hit.getSide();
            if (side == Direction.UP || side == Direction.DOWN)
                return ActionResult.PASS;

            BlockPos hitPos = hit.getBlockPos();
            BlockState hitState = RunePortals.FRAME_BLOCK.getDefaultState()
                .with(FrameBlock.FACING, side)
                .with(FrameBlock.RUNE, fragment.getRuneValue());

            world.setBlockState(hitPos, hitState, 3);
            world.playSound(null, hitPos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);

            if (!player.getAbilities().creativeMode)
                held.decrement(1);

            return ActionResult.CONSUME;
        }

        return ActionResult.PASS;
    }
}
