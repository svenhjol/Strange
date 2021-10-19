package svenhjol.strange.module.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.knowledge.Destination;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeData;
import svenhjol.strange.module.runestones.enums.IRunestoneMaterial;

import javax.annotation.Nullable;
import java.util.Random;

public class RunestoneBlock extends CharmBlockWithEntity {
    private final IRunestoneMaterial material;

    public RunestoneBlock(CharmModule module, IRunestoneMaterial material) {
        super(module, material.getSerializedName() + "_runestone", material.getProperties());
        this.material = material;
    }

    public IRunestoneMaterial getMaterial() {
        return material;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RunestoneBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!level.isClientSide) {
            boolean result = tryStudyRunestone((ServerLevel)level, pos, player);

            if (!result) {
                return InteractionResult.FAIL;
            }

            player.openMenu(state.getMenuProvider(level, pos));
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        RunestoneBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity != null) {
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
    private boolean tryStudyRunestone(ServerLevel level, BlockPos pos, Player player) {
        RunestoneBlockEntity runestone = getBlockEntity(level, pos);
        if (runestone == null) {
            return false;
        }

        KnowledgeData knowledgeData = Knowledge.getSavedData().orElseThrow();
        Destination destination;

        // try and generate a new destination if this runestone hasn't been set up or if there's no recorded destination
        if (runestone.runes == null || runestone.runes.isEmpty() || !knowledgeData.hasDestination(runestone.runes)) {

            // update the runestone with the generated runes. If runestone.location is null, a location will be generated from the difficulty
            destination = RunestoneLocations.createDestination(DimensionHelper.getDimension(level), new Random(pos.asLong()), runestone.difficulty, runestone.decay, runestone.location)
                .orElseThrow();

            runestone.runes = destination.runes;
            runestone.setChanged();
        }

        destination = knowledgeData.getDestination(runestone.runes).orElseThrow();
        CompoundTag tag = destination.toTag();

        // send the destination tag to the player who looked at the runestone
        NetworkHelper.sendPacketToClient((ServerPlayer) player, Runestones.MSG_CLIENT_SET_ACTIVE_DESTINATION, buf
            -> buf.writeNbt(tag));

        return true;
    }
}
