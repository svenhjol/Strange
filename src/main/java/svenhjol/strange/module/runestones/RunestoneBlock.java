package svenhjol.strange.module.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import svenhjol.charm.block.CharmBlockWithEntity;
import svenhjol.charm.helper.EnchantmentsHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.api.event.InteractDiscoveryCallback;
import svenhjol.strange.module.discoveries.Discoveries;
import svenhjol.strange.module.discoveries.Discovery;
import svenhjol.strange.module.discoveries.DiscoveryHelper;
import svenhjol.strange.module.runestone_dust.RunestoneDust;

import javax.annotation.Nullable;
import java.util.Random;

@SuppressWarnings("deprecation")
public class RunestoneBlock extends CharmBlockWithEntity {
    private final RunestoneMaterial material;

    public RunestoneBlock(CharmModule module, RunestoneMaterial material) {
        super(module, material.getSerializedName() + "_runestone", material.getProperties());
        this.material = material;
    }

    public RunestoneMaterial getMaterial() {
        return material;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RunestoneBlockEntity(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState state2, boolean bl) {
        super.onPlace(state, level, pos, state2, bl);
        RunestoneBlockEntity runestone = getBlockEntity(level, pos);
        if (runestone != null) {
            var random = new Random(pos.asLong() + runestone.hashCode());
            runestone.difficulty = random.nextFloat();
            runestone.setChanged();
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!level.isClientSide) {
            boolean result = tryReadRunestone((ServerPlayer) player, pos);
            if (!result) return InteractionResult.FAIL;
            player.openMenu(state.getMenuProvider(level, pos));
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        RunestoneBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity != null) {

            // Drop runestone dust if enabled. Fortune affects number of drops.
            if (Strange.LOADER.isEnabled(RunestoneDust.class)) {
                var drops = 1 + level.random.nextInt((EnchantmentsHelper.getFortune(player) * 3) + 3);
                for (int i = 0; i < drops; i++) {
                    level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(RunestoneDust.RUNESTONE_DUST)));
                }
            }

            ItemStack stack = blockEntity.getItem(0);
            ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY() + 0.5D, pos.getZ(), stack);
            level.addFreshEntity(itemEntity);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Nullable
    public RunestoneBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof RunestoneBlockEntity) {
            return (RunestoneBlockEntity) blockEntity;
        }
        return null;
    }

    /**
     * Fetch or generate a destination when a player looks at a runestone.
     */
    private boolean tryReadRunestone(ServerPlayer player, BlockPos pos) {
        var level = (ServerLevel) player.level;
        RunestoneBlockEntity runestone = getBlockEntity(level, pos);
        if (runestone == null) return false;

        var discoveries = Discoveries.getDiscoveries().orElse(null);
        if (discoveries == null) return false;
        boolean generate = false;

        if (runestone.runes == null || runestone.runes.isEmpty()) {

            // The runestone has no runes. We generate a new discovery and store its runes within this runestone.
            // Next time a player activates this runestone we can lookup the discovery from its runes.
            generate = true;

        } else if (!discoveries.contains(runestone.runes)) {

            // The runestone has runes that do not exist in the saved discoveries.
            // This means that we need to regenerate this location and store it back into the discoveries.
            LogHelper.warn(getClass(), "Runestone data was erased for this runestone, regenerating it.");
            generate = true;

        }

        if (generate) {
            var random = new Random(pos.asLong());
            var location = runestone.location;
            var difficulty = runestone.difficulty;

            // Try and generate a discovery from this location and difficulty, then update the runestone's runes to match it.
            Discovery discovery = DiscoveryHelper.getOrCreate(level, difficulty, pos, random, location, player);
            if (discovery == null) return false;

            runestone.runes = discovery.getRunes();
            runestone.setChanged();
        }

        // At this point we should be able to fetch the discovery that matches the runestone's runes.
        var discovery = discoveries.get(runestone.runes);

        if (discovery == null) {
            LogHelper.warn(getClass(), "The runestone doesn't refer to a valid destination, giving up.");
            return false;
        }

        // Inform the player's client that they looked at this specific runestone.
        InteractDiscoveryCallback.EVENT.invoker().interact(player, discovery);
        return true;
    }
}
