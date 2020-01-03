package svenhjol.strange.spells.spells;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

import java.util.function.Consumer;

public class RiseSpell extends Spell
{
    public RiseSpell()
    {
        super("rise");
        this.color = DyeColor.LIGHT_GRAY;
        this.affect = Affect.SELF;
        this.applyCost = 2;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack stone, Consumer<Boolean> didCast)
    {
        this.castSelf(player, p -> {
            p.addPotionEffect(new EffectInstance(Effects.SLOW_FALLING, 350, 0));
            p.addPotionEffect(new EffectInstance(Effects.LEVITATION, 150, 2));

            didCast.accept(true);
        });
    }
}
