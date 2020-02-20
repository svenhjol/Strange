package svenhjol.strange.spells.spells;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BoostSpell extends Spell
{
    public BoostSpell()
    {
        super("boost");
        this.color = DyeColor.BLUE;
        this.affect = Affect.AREA;
        this.applyCost = 2;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack stone, Consumer<Boolean> didCast)
    {
        int[] range = {8, 5, 8};
        this.castArea(player, range, blocks -> {
            World world = player.world;
            if (world.isRemote) return;

            List<LivingEntity> entities;
            AxisAlignedBB area = player.getBoundingBox().grow(range[0], range[1], range[2]);
            Predicate<LivingEntity> selector = entity -> entity != null && !(entity instanceof MonsterEntity);
            entities = world.getEntitiesWithinAABB(LivingEntity.class, area, selector);

            if (!entities.isEmpty()) {
                for (LivingEntity entity : entities) {
                    Collection<EffectInstance> effects = entity.getActivePotionEffects();

                    for (EffectInstance effect : effects) {
                        int duration = (int) (effect.getDuration() * 1.2);
                        int amplifier = effect.getAmplifier() + 1;
                        entity.addPotionEffect(new EffectInstance(effect.getPotion(), duration, amplifier));
                    }
                }
            }

            didCast.accept(true);
        });
    }
}
