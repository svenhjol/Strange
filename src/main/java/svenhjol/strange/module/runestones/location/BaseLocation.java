package svenhjol.strange.module.runestones.location;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.border.WorldBorder;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.knowledge.Destination;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeData;
import svenhjol.strange.module.runestones.RunestoneBlockEntity;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public abstract class BaseLocation {
    protected final ResourceLocation location;
    protected float difficulty;

    public BaseLocation(ResourceLocation location, float difficulty) {
        this.location = location;
        this.difficulty = difficulty;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public float getDifficulty() {
        return difficulty;
    }

    public abstract BlockPos getDestination(ServerLevel world, BlockPos startPos, int maxDistance, Random random, @Nullable ServerPlayer player);

    protected BlockPos checkBounds(Level world, BlockPos pos) {
        WorldBorder border = world.getWorldBorder();

        if (pos.getX() > border.getMaxX())
            pos = new BlockPos(border.getMaxX(), pos.getY(), pos.getZ());

        if (pos.getX() < border.getMinX())
            pos = new BlockPos(border.getMinX(), pos.getY(), pos.getZ());

        if (pos.getZ() > border.getMaxZ())
            pos = new BlockPos(pos.getX(), pos.getY(), border.getMaxZ());

        if (pos.getZ() < border.getMinZ())
            pos = new BlockPos(pos.getX(), pos.getY(), border.getMinZ());

        return pos;
    }

    @Nullable
    protected BlockPos tryLoad(Level level, BlockPos runePos) {
        BlockEntity blockEntity = level.getBlockEntity(runePos);
        if (blockEntity instanceof RunestoneBlockEntity runestone) {
            String runes = runestone.runes;

            KnowledgeData knowledge = Knowledge.getSavedData().orElseThrow();
            Optional<Destination> optDest = knowledge.destinations.get(runes);
            if (optDest.isEmpty()) return null;

            Destination destination = optDest.get();
            if (destination.pos == BlockPos.ZERO) {
                return null;
            }

            if (DimensionHelper.isDimension(level, destination.dimension)) {
                LogHelper.debug(this.getClass(), "Runestone has location " + destination.location + " with position " + destination.pos);
                return destination.pos;
            }
        }

        return null;
    }

    protected void store(Level level, BlockPos runePos, BlockPos storePos, @Nullable Player player) {
        BlockEntity blockEntity = level.getBlockEntity(runePos);
        if (blockEntity instanceof RunestoneBlockEntity runestone) {
            String runes = runestone.runes;

            KnowledgeData knowledge = Knowledge.getSavedData().orElseThrow();
            knowledge.destinations.get(runes).ifPresent(dest -> {
                dest.pos = storePos;
                dest.player = player != null ? player.getName().getContents() : "";
            });
            runestone.setChanged();
            LogHelper.debug(this.getClass(), "Stored position " + storePos + " in runestone");
        }
    }
}