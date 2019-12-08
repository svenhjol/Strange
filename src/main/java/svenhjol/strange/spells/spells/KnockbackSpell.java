package svenhjol.strange.spells.spells;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class KnockbackSpell extends Spell
{
    public KnockbackSpell()
    {
        super("knockback");
        this.element = Element.AIR;
        this.affect = Affect.TARGET;
        this.duration = 1.0F;
        this.castCost = 4;
        this.quantity = 50;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack staff, Consumer<Boolean> didCast)
    {
        this.castTarget(player, (result, beam) -> {
            Entity e = getClosestEntity(player.world, result);
            beam.remove();
            if (e instanceof LivingEntity) {
                beam.remove();
                LivingEntity living = (LivingEntity) e;
                living.knockBack(player, 6.0F, MathHelper.sin(player.rotationYaw * ((float)Math.PI / 180F)), -MathHelper.cos(player.rotationYaw * ((float)Math.PI / 180F)));
                didCast.accept(true);
                return;
            }
            didCast.accept(false);
        });
    }
}
