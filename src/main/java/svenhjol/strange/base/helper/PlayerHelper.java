package svenhjol.strange.base.helper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlayerHelper {
    public static void teleport(World world, BlockPos pos, PlayerEntity player) {
        if (!world.isClient) {
            player.requestTeleport(pos.getX() + 0.5D, pos.getY() + 0.25D, pos.getZ() + 0.5D);
            world.sendEntityStatus(player, (byte) 46);
            world.playSound(null, pos, SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.5F, 1.25F);
        }
    }
}
