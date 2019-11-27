package svenhjol.strange.magic.spells;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

import java.util.function.Consumer;

public class SlownessSpell extends Spell
{
    public SlownessSpell()
    {
        super("slowness");
        this.element = Element.WATER;
        this.affect = Affect.TARGET;
        this.duration = 1.0F;
        this.castCost = 10;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack staff, Consumer<Boolean> didCast)
    {
        this.castTarget(player, (result, beam) -> {
            if (result.getType() == RayTraceResult.Type.ENTITY) {
                EntityRayTraceResult entityImpact = (EntityRayTraceResult) result;
                Entity e = entityImpact.getEntity();
                if (!e.isEntityEqual(player) && e instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) e;
                    living.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 120, 1));
                    beam.remove();
                    didCast.accept(true);
                    return;
                }
            }
            didCast.accept(false);
        });
    }
}
