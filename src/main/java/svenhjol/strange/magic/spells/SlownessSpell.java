package svenhjol.strange.magic.spells;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

public class SlownessSpell extends Spell
{
    public SlownessSpell()
    {
        super("slowness");
        this.element = Element.WATER;
        this.affect = Affect.TARGET;
        this.duration = 30;
        this.xpCost = 20;
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
                    living.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 120, 1));
                    beam.remove();
                }
            }
        });

        return true;
    }
}
