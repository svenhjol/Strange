package svenhjol.strange.runeportals;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.runestones.Runestones;

import javax.annotation.Nullable;

@Module(mod = Strange.MOD_ID, client = RunePortalsClient.class)
public class RunePortals extends CharmModule {
    public static FrameBlock FRAME_BLOCK;
    public static WolfBlock WOLF_BLOCK;

    public static final Identifier RUNE_PORTAL_BLOCK_ID = new Identifier(Strange.MOD_ID, "rune_portal");
    public static RunePortalBlock RUNE_PORTAL_BLOCK;
    public static BlockEntityType<RunePortalBlockEntity> RUNE_PORTAL_BLOCK_ENTITY;

    @Override
    public void register() {
        FRAME_BLOCK = new FrameBlock(this);
        WOLF_BLOCK = new WolfBlock(this);
        RUNE_PORTAL_BLOCK = new RunePortalBlock(this);
        RUNE_PORTAL_BLOCK_ENTITY = RegistryHandler.blockEntity(RUNE_PORTAL_BLOCK_ID, RunePortalBlockEntity::new, RUNE_PORTAL_BLOCK);
    }

    @Override
    public void init() {
        // listen for broken frames
        PlayerBlockBreakEvents.BEFORE.register(this::handleBlockBreak);
    }

    public static void breakSurroundingPortals(World world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos blockpos = pos.offset(direction);
            BlockState s = world.getBlockState(blockpos);
            if (s.getBlock() instanceof RunePortalBlock)
                ((RunePortalBlock)s.getBlock()).remove(world, pos);
        }
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