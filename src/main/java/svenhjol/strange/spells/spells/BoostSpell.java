package svenhjol.strange.spells.spells;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;

import java.util.Collection;
import java.util.function.Consumer;

public class BoostSpell extends Spell
{
    public BoostSpell()
    {
        super("boost");
        this.element = Element.WATER;
        this.affect = Affect.SELF;
        this.applyCost = 5;
        this.duration = 3.0F;
        this.castCost = 20;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack staff, Consumer<Boolean> didCast)
    {
        this.castSelf(player, p -> {
            Collection<EffectInstance> effects = p.getActivePotionEffects();

            for (EffectInstance effect : effects) {
                int duration = (int)(effect.getDuration() * 1.2);
                int amplifier = effect.getAmplifier() + 1;
                p.addPotionEffect(new EffectInstance(effect.getPotion(), duration, amplifier));
            }

            didCast.accept(true);
        });
    }
}
