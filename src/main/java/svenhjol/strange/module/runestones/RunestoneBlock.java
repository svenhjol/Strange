package svenhjol.strange.module.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.knowledge.Destination;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeData;
import svenhjol.strange.module.runestones.enums.IRunestoneMaterial;

import javax.annotation.Nullable;
import java.util.Optional;
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
            boolean result = tryReadRunestone((ServerLevel)level, pos, (ServerPlayer) player);

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
    private boolean tryReadRunestone(ServerLevel level, BlockPos pos, ServerPlayer player) {
        RunestoneBlockEntity runestone = getBlockEntity(level, pos);
        if (runestone == null) {
            return false;
        }

        KnowledgeData knowledge = Knowledge.getKnowledgeData().orElseThrow();
        Destination destination;
        boolean generate = false;

        if (runestone.runes == null || runestone.runes.isEmpty()) {
            generate = true;
        } else if (!knowledge.specials.has(runestone.runes) && !knowledge.destinations.has(runestone.runes)) {
            LogHelper.warn(this.getClass(), "Runestone data was erased for this runestone, regenerating it");
            generate = true; // the knowledgedata was erased for this runestone - regenerate it
        }

        if (generate) {
            ResourceLocation location = runestone.location;
            ResourceLocation dimension = DimensionHelper.getDimension(level);
            Random random = new Random(pos.asLong());
            float difficulty = runestone.difficulty;
            float decay = runestone.decay;

            // If runestone.location is null, a location will be generated from the difficulty.
            destination = RunestoneHelper.getOrCreateDestination(dimension, random, difficulty, decay, location).orElseThrow();

            runestone.runes = destination.getRunes();
            runestone.setChanged();
        }

        Optional<Destination> optSpawn = knowledge.specials.get(runestone.runes);
        Optional<Destination> optDest = knowledge.destinations.get(runestone.runes);

        if (optSpawn.isPresent()) {
            destination = optSpawn.get();
        } else if (optDest.isPresent()) {
            destination = optDest.get();
        } else {
            LogHelper.error(this.getClass(), "The runestone doesn't refer to spawn or a destination, giving up");
            return false;
        }

        // send the destination tag to the player who looked at the runestone
        CompoundTag tag = destination.toTag();
        NetworkHelper.sendPacketToClient(player, Runestones.MSG_CLIENT_SET_DESTINATION, buf -> buf.writeNbt(tag));

        return true;
    }
}
