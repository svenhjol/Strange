package svenhjol.strange.magic.spells;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import svenhjol.meson.Meson;

public class KnockbackSpell extends Spell
{
    public KnockbackSpell()
    {
        super("knockback");
        this.element = Element.AIR;
        this.effect = Effect.TARGET;
        this.duration = 20;
    }

    @Override
    public boolean cast(PlayerEntity player, ItemStack staff)
    {
        this.castTarget(player, impactResult -> {
            if (impactResult.getType() == RayTraceResult.Type.ENTITY) {
                EntityRayTraceResult entityImpact = (EntityRayTraceResult) impactResult;
                Entity e = entityImpact.getEntity();
                if (!e.isEntityEqual(player) && e instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) e;
                    Meson.log("Hit");

                    living.knockBack(player, 6.0F, (double)MathHelper.sin(player.rotationYaw * ((float)Math.PI / 180F)), (double)(-MathHelper.cos(player.rotationYaw * ((float)Math.PI / 180F))));
                }
//                entity.remove();
            }
        });

        return true;
    }
}
