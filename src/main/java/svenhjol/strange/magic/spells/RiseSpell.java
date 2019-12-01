package svenhjol.strange.magic.spells;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

import java.util.function.Consumer;

public class RiseSpell extends Spell
{
    public RiseSpell()
    {
        super("rise");
        this.element = Element.AIR;
        this.affect = Affect.SELF;
        this.applyCost = 3;
        this.duration = 3.0F;
        this.castCost = 10;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack staff, Consumer<Boolean> didCast)
    {
        this.castSelf(player, p -> {
            p.addPotionEffect(new EffectInstance(Effects.SLOW_FALLING, 350, 0));
            p.addPotionEffect(new EffectInstance(Effects.LEVITATION, 150, 2));

            didCast.accept(true);
        });
    }
}
