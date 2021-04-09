package svenhjol.strange.runeportals;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.block.CharmBlock;

public abstract class BaseFrameBlock extends CharmBlock {
    public BaseFrameBlock(CharmModule module, String name, Settings props) {
        super(module, name, props);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient)
            return ActionResult.PASS;

        ItemStack held = player.getStackInHand(hand);



        return ActionResult.PASS;
    }


}
