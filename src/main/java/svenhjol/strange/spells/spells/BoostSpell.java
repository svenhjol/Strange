package svenhjol.strange.spells.spells;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;

import java.util.Collection;
import java.util.function.Consumer;

public class BoostSpell extends Spell
{
    public BoostSpell()
    {
        super("boost");
        this.color = DyeColor.BLUE;
        this.affect = Affect.SELF;
        this.applyCost = 2;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack stone, Consumer<Boolean> didCast)
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
