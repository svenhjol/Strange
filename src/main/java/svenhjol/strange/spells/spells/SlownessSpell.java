package svenhjol.strange.spells.spells;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
        int[] range = {10, 3, 10};
        this.castArea(player, range, blocks -> {
            World world = player.world;
            if (world.isRemote) return;

            List<LivingEntity> entities;
            AxisAlignedBB area = player.getBoundingBox().grow(range[0], range[1], range[2]);
            Predicate<LivingEntity> selector = entity -> entity != null && !entity.isEntityEqual(player);
            entities = world.getEntitiesWithinAABB(LivingEntity.class, area, selector);

            if (!entities.isEmpty()) {
                for (LivingEntity entity : entities) {
                    entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 150, 3));
                }
            }

            didCast.accept(true);
        });
    }
}
