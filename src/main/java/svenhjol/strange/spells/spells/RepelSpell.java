package svenhjol.strange.spells.spells;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RepelSpell extends Spell
{
    public RepelSpell()
    {
        super("repel");
        this.color = DyeColor.PURPLE;
        this.affect = Affect.AREA;
        this.uses = 3;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack stone, Consumer<Boolean> didCast)
    {
        int[] range = {12, 3, 12};
        this.castArea(player, range, blocks -> {
            World world = player.world;

            if (world.isRemote) return;

            List<LivingEntity> entities;
            AxisAlignedBB area = player.getBoundingBox().grow(range[0], range[1], range[2]);
            Predicate<LivingEntity> selector = Objects::nonNull;
            entities = world.getEntitiesWithinAABB(LivingEntity.class, area, selector);
            if (!entities.isEmpty()) {
                for (LivingEntity entity : entities) {
                    entity.knockBack(player, 6.0F, MathHelper.sin(player.rotationYaw * ((float)Math.PI / 180F)), -MathHelper.cos(player.rotationYaw * ((float)Math.PI / 180F)));
                }
            }
            didCast.accept(true);
        });
    }
}
