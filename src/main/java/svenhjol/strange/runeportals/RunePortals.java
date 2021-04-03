package svenhjol.strange.runeportals;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.runestones.Runestones;

import javax.annotation.Nullable;

@Module(mod = Strange.MOD_ID)
public class RunePortals extends CharmModule {
    public static FrameBlock FRAME_BLOCK;
    public static WolfBlock WOLF_BLOCK;

    @Override
    public void register() {
        FRAME_BLOCK = new FrameBlock(this);
        WOLF_BLOCK = new WolfBlock(this);
    }

    @Override
    public void init() {
        // listen for broken frames
        PlayerBlockBreakEvents.BEFORE.register(this::handleBlockBreak);
    }

    private boolean handleBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        if (!(state.getBlock() instanceof FrameBlock))
            return true;

        int runeValue = state.get(FrameBlock.RUNE);
        ItemStack drop = new ItemStack(Runestones.RUNIC_FRAGMENTS.get(runeValue));
        world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), drop));

        return true;
    }
}
