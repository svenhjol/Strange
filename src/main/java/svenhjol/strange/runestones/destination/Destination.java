package svenhjol.strange.runestones.destination;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import svenhjol.charm.Charm;
import svenhjol.strange.runestones.RunestoneBlockEntity;
import svenhjol.strange.runestones.RunestoneHelper;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class Destination {
    protected final Identifier location;
    protected float weight;

    public Destination(Identifier location, float weight) {
        this.location = location;
        this.weight = weight;
    }

    public boolean isSpawnPoint() {
        return this.location.equals(RunestoneHelper.SPAWN);
    }

    public Identifier getLocation() {
        return location;
    }

    public float getWeight() {
        return weight;
    }

    public abstract BlockPos getDestination(ServerWorld world, BlockPos runePos, Random random, @Nullable ServerPlayerEntity player);

    protected BlockPos checkBounds(World world, BlockPos pos) {
        WorldBorder border = world.getWorldBorder();

        if (pos.getX() > border.getBoundEast())
            pos = new BlockPos(border.getBoundEast(), pos.getY(), pos.getZ());

        if (pos.getX() < border.getBoundWest())
            pos = new BlockPos(border.getBoundWest(), pos.getY(), pos.getZ());

        if (pos.getZ() > border.getBoundSouth())
            pos = new BlockPos(pos.getX(), pos.getY(), border.getBoundSouth());

        if (pos.getZ() < border.getBoundNorth())
            pos = new BlockPos(pos.getX(), pos.getY(), border.getBoundNorth());

        return pos;
    }

    @Nullable
    protected BlockPos tryLoad(World world, BlockPos runePos) {
        BlockEntity blockEntity = world.getBlockEntity(runePos);
        if (blockEntity instanceof RunestoneBlockEntity) {
            RunestoneBlockEntity runeBlockEntity = (RunestoneBlockEntity)blockEntity;
            BlockPos position = runeBlockEntity.position;
            Identifier location = runeBlockEntity.location;

            if (location != null && position != null) {
                Charm.LOG.debug("Found destination in runestone: " + location.toString());
                return position;
            }
        }

        return null;
    }

    protected void store(World world, BlockPos runePos, BlockPos storePos, @Nullable PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(runePos);
        if (blockEntity instanceof RunestoneBlockEntity) {
            RunestoneBlockEntity runeBlockEntity = (RunestoneBlockEntity)blockEntity;
            runeBlockEntity.position = storePos;
            runeBlockEntity.location = this.location;
            runeBlockEntity.player = player != null ? player.getName().asString() : "";
            runeBlockEntity.markDirty();

            Charm.LOG.debug("Stored location in runestone for next use");
        }
    }
}