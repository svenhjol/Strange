package svenhjol.strange.magic.spells;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;

import java.util.function.Consumer;

public class KnockbackSpell extends Spell
{
    public KnockbackSpell()
    {
        super("knockback");
        this.element = Element.AIR;
        this.affect = Affect.TARGET;
        this.duration = 1.0F;
        this.castCost = 5;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack staff, Consumer<Boolean> didCast)
    {
        this.castTarget(player, (result, beam) -> {
            if (result.getType() == RayTraceResult.Type.ENTITY) {
                EntityRayTraceResult entityImpact = (EntityRayTraceResult) result;
                Entity e = entityImpact.getEntity();
                beam.remove();
                if (!e.isEntityEqual(player) && e instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) e;
                    living.knockBack(player, 6.0F, MathHelper.sin(player.rotationYaw * ((float)Math.PI / 180F)), -MathHelper.cos(player.rotationYaw * ((float)Math.PI / 180F)));
                    didCast.accept(true);
                    return;
                }
            }
            didCast.accept(false);
        });
    }
}
