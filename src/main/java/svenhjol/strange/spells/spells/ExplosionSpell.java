package svenhjol.strange.spells.spells;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;

import java.util.function.Consumer;

public class ExplosionSpell extends Spell
{
    public ExplosionSpell()
    {
        super("explosion");
        this.color = DyeColor.ORANGE;
        this.affect = Affect.TARGET;
        this.applyCost = 3;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack stone, Consumer<Boolean> didCast)
    {
        this.castTarget(player, (result, beam) -> {
            BlockPos pos = null;

            if (result.getType() == RayTraceResult.Type.BLOCK) {
                pos = ((BlockRayTraceResult) result).getPos();
            } else if (result.getType() == RayTraceResult.Type.ENTITY) {
                pos = ((EntityRayTraceResult) result).getEntity().getPosition();
            }

            if (pos != null) {
                beam.remove();
                player.world.createExplosion(player, pos.getX(), pos.getY(), pos.getZ(), 4.0F, Explosion.Mode.BREAK);
                didCast.accept(true);
                return;
            }

            didCast.accept(false);
        });
    }
}
