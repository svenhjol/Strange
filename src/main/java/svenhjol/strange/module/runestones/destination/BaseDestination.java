package svenhjol.strange.module.runestones.destination;

import svenhjol.charm.Charm;
import svenhjol.strange.module.runestones.RunestoneBlockEntity;
import svenhjol.strange.module.runestones.RunestonesHelper;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.border.WorldBorder;
import java.util.Random;

public abstract class BaseDestination {
    protected final ResourceLocation location;
    protected float weight;

    public BaseDestination(ResourceLocation location, float weight) {
        this.location = location;
        this.weight = weight;
    }

    public boolean isSpawnPoint() {
        return this.location.equals(RunestonesHelper.SPAWN);
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public float getWeight() {
        return weight;
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
    protected BlockPos tryLoad(Level world, BlockPos runePos) {
        BlockEntity blockEntity = world.getBlockEntity(runePos);
        if (blockEntity instanceof RunestoneBlockEntity) {
            RunestoneBlockEntity runeBlockEntity = (RunestoneBlockEntity)blockEntity;
            BlockPos position = runeBlockEntity.position;
            ResourceLocation location = runeBlockEntity.location;

            if (location != null && position != null) {
                Charm.LOG.debug("Found destination in runestone: " + location.toString());
                return position;
            }
        }

        return null;
    }

    protected void store(Level world, BlockPos runePos, BlockPos storePos, @Nullable Player player) {
        BlockEntity blockEntity = world.getBlockEntity(runePos);
        if (blockEntity instanceof RunestoneBlockEntity) {
            RunestoneBlockEntity runeBlockEntity = (RunestoneBlockEntity)blockEntity;
            runeBlockEntity.position = storePos;
            runeBlockEntity.location = this.location;
            runeBlockEntity.player = player != null ? player.getName().getContents() : "";
            runeBlockEntity.setChanged();

            Charm.LOG.debug("Stored location in runestone for next use");
        }
    }
}