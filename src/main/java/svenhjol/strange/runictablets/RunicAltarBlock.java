package svenhjol.strange.runictablets;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.block.CharmBlock;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.strange.runestones.RunestoneHelper;

import javax.annotation.Nullable;

public class RunicAltarBlock extends CharmBlock {
    public static final Text TITLE = new TranslatableText("container.strange.runic_altar");

    public RunicAltarBlock(CharmModule module) {
        super(module, RunicTablets.BLOCK_ID.getPath(), Settings.copy(Blocks.STONE));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient || !ModuleHandler.enabled("strange:runic_tablets")) {
            return ActionResult.SUCCESS;
        } else {
            // ensure client has the latest rune discoveries
            RunestoneHelper.syncLearnedRunesToClient((ServerPlayerEntity)player);
            player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            return ActionResult.CONSUME;
        }
    }

    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity)
            -> new RunicAltarScreenHandler(i, playerInventory, ScreenHandlerContext.create(world, pos)), TITLE);
    }

}
