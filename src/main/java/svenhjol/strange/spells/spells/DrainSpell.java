package svenhjol.strange.spells.spells;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DrainSpell extends Spell
{
    public DrainSpell()
    {
        super("drain");
        this.color = DyeColor.BLACK;
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
            Predicate<LivingEntity> selector = e -> !e.isEntityEqual(player);
            entities = world.getEntitiesWithinAABB(LivingEntity.class, area, selector);

            float drained = 0.0F;

            if (!entities.isEmpty()) {
                for (LivingEntity entity : entities) {
                    boolean result = entity.attackEntityFrom(DamageSource.MAGIC, 2.0F);
                    if (result) {
                        drained += 2.0F;
                    }
                }
            }

            player.heal(drained);
        });

        didCast.accept(true);
    }
}
