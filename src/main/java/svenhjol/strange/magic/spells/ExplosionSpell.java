package svenhjol.strange.magic.spells;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class ExplosionSpell extends Spell
{
    public ExplosionSpell()
    {
        super("explosion");
        this.element = Element.FIRE;
        this.affect = Affect.TARGET;
        this.duration = 80;
        this.xpCost = 100;
    }

    @Override
    public boolean cast(PlayerEntity player, ItemStack staff)
    {
        this.castTarget(player, (result, beam) -> {
            BlockPos pos = null;

            if (result.getType() == RayTraceResult.Type.BLOCK) {
                pos = ((BlockRayTraceResult) result).getPos();
            } else if (result.getType() == RayTraceResult.Type.ENTITY && !((EntityRayTraceResult)result).getEntity().isEntityEqual(player)) {
                pos = ((EntityRayTraceResult) result).getEntity().getPosition();
            }

            if (pos != null) {
                World world = player.world;
                world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 4.0F, Explosion.Mode.BREAK);
                beam.remove();
            }
        });

        return true;
    }
}
