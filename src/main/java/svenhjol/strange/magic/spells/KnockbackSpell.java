package svenhjol.strange.magic.spells;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;

public class KnockbackSpell extends Spell
{
    public KnockbackSpell()
    {
        super("knockback");
        this.element = Element.AIR;
        this.affect = Affect.TARGET;
        this.duration = 20;
        this.xpCost = 30;
    }

    @Override
    public boolean cast(PlayerEntity player, ItemStack book)
    {
        this.castTarget(player, (result, beam) -> {
            if (result.getType() == RayTraceResult.Type.ENTITY) {
                EntityRayTraceResult entityImpact = (EntityRayTraceResult) result;
                Entity e = entityImpact.getEntity();
                if (!e.isEntityEqual(player) && e instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) e;
                    living.knockBack(player, 6.0F, (double)MathHelper.sin(player.rotationYaw * ((float)Math.PI / 180F)), (double)(-MathHelper.cos(player.rotationYaw * ((float)Math.PI / 180F))));
                    beam.remove();
                }
            }
        });

        return true;
    }
}
