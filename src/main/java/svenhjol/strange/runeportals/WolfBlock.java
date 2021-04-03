package svenhjol.strange.runeportals;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.block.CharmBlock;
import svenhjol.strange.runestones.RunicFragmentItem;

public class WolfBlock extends CharmBlock {
    public WolfBlock(CharmModule module) {
        super(module, "wolf", FabricBlockSettings.copy(Blocks.STONE));
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
            held.decrement(1);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
