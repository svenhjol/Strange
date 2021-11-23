package svenhjol.strange.module.experience_bottles;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ExperienceBottleProjectile extends ThrowableItemProjectile {
    public ExperienceBottleProjectile(EntityType<? extends ExperienceBottleProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public ExperienceBottleProjectile(Level level, LivingEntity livingEntity) {
        super(ExperienceBottles.EXPERIENCE_BOTTLE, livingEntity, level);
    }

    public ExperienceBottleProjectile(Level level, double d, double e, double f) {
        super(ExperienceBottles.EXPERIENCE_BOTTLE, d, e, f, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.EXPERIENCE_BOTTLE;
    }

    @Override
    protected float getGravity() {
        return 0.07F;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (level instanceof ServerLevel) {
            level.levelEvent(2002, blockPosition(), PotionUtils.getColor(Potions.WATER));
            ExperienceBottleItem item = (ExperienceBottleItem)getItem().getItem();
            int orbs = item.getType().getOrbs();
            ExperienceOrb.award((ServerLevel)level, position(), orbs);
            discard();
        }
    }
}
