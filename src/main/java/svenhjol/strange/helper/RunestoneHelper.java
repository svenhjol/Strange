package svenhjol.strange.helper;

import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import svenhjol.meson.helper.StringHelper;
import svenhjol.strange.Strange;

import javax.annotation.Nullable;
import java.util.Random;

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

    public static BlockPos addRandomOffset(BlockPos pos, Random rand, int max) {
        int n = rand.nextInt(max);
        int e = rand.nextInt(max);
        int s = rand.nextInt(max);
        int w = rand.nextInt(max);
        pos = pos.north(rand.nextFloat() < 0.5F ? n : -n);
        pos = pos.east(rand.nextFloat() < 0.5F ? e : -e);
        pos = pos.south(rand.nextFloat() < 0.5F ? s : -s);
        pos = pos.west(rand.nextFloat() < 0.5F ? w : -w);
        return pos;
    }

    public static String getFormattedStructureName(Identifier structureId) {
        return StringHelper.capitalize(structureId.getPath().replaceAll("_", " "));
    }
}
