package svenhjol.strange.spells.spells;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.EntityRayTraceResult;

import java.util.function.Consumer;

public class SlownessSpell extends Spell
{
    public SlownessSpell()
    {
        super("slowness");
        this.color = DyeColor.LIGHT_BLUE;
        this.affect = Affect.TARGET;
        this.uses = 3;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack stone, Consumer<Boolean> didCast)
    {
        this.castTarget(player, (result, beam) -> {
            EntityRayTraceResult entityImpact = (EntityRayTraceResult) result;
            Entity e = entityImpact.getEntity();
            beam.remove();

            if (!e.isEntityEqual(player) && e instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) e;
                living.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 150, 3));
                didCast.accept(true);
                return;
            }
            didCast.accept(false);
        });
    }
}
