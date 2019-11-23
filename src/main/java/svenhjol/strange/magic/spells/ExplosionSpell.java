package svenhjol.strange.magic.spells;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class ExplosionSpell extends Spell
{
    public ExplosionSpell()
    {
        super("explosion");
        this.element = Element.FIRE;
        this.effect = Effect.TARGET;
        this.duration = 80;
        this.xpCost = 100;
    }

    @Override
    public boolean cast(PlayerEntity player, ItemStack staff)
    {
        this.castTarget(player, impactResult -> {
            if (impactResult.getType() == RayTraceResult.Type.BLOCK) {
                World world = player.world;
                BlockRayTraceResult impact = (BlockRayTraceResult) impactResult;
                BlockPos pos = impact.getPos();

                world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 5.0F, Explosion.Mode.BREAK);
            }
        });

        return true;
    }
}
