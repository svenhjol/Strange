package svenhjol.strange.module.runestones.destination;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.Strange;

import java.util.Random;

public class SpawnDestination extends BaseDestination {
    public static final ResourceLocation SPAWN = new ResourceLocation(Strange.MOD_ID, "spawn");

    public SpawnDestination() {
        super(SPAWN, 1.0F);
    }

    @Override
    public BlockPos getDestination(ServerLevel world, BlockPos startPos, int maxDistance, Random random, @Nullable ServerPlayer player) {
        return world.getSharedSpawnPos();
    }
}
