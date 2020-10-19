package svenhjol.strange.helper;

import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import svenhjol.charm.base.helper.StringHelper;
import svenhjol.strange.Strange;

import javax.annotation.Nullable;

public class RunestoneHelper {
    public static final Identifier SPAWN = new Identifier(Strange.MOD_ID, "spawn_point");

    public static boolean explode(World world, BlockPos pos, @Nullable PlayerEntity player, boolean destroyBlock) {
        if (player != null)
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 5 * 20));

        world.createExplosion(null, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 1.25F, Explosion.DestructionType.DESTROY);

        if (destroyBlock)
            world.setBlockState(pos, Blocks.AIR.getDefaultState());

        return false;
    }

    public static String getFormattedLocationName(Identifier locationId) {
        return StringHelper.capitalize(locationId.getPath().replaceAll("_", " "));
    }
}
