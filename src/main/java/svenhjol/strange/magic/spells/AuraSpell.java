package svenhjol.strange.magic.spells;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class AuraSpell extends Spell
{
    public AuraSpell()
    {
        super("aura");
        this.element = Element.WATER;
        this.affect = Affect.AREA;
        this.quantity = 8;
        this.duration = 3.5F;
        this.castCost = 20;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack staff, Consumer<Boolean> didCast)
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
                    float current = entity.getHealth();
                    entity.setHealth(current + 10.0F);
                }
            }
        });

        didCast.accept(true);
    }
}
