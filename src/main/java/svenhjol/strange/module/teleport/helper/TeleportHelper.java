package svenhjol.strange.module.teleport.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;

public class TeleportHelper {
    public static void explode(ServerLevel level, BlockPos pos, float size, Explosion.BlockInteraction interaction) {
        level.explode(null, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, size, interaction);
    }
}
