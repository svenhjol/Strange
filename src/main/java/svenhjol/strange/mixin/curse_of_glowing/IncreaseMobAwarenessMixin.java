package svenhjol.strange.mixin.curse_of_glowing;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import svenhjol.strange.module.curse_of_glowing.CurseOfGlowing;

@Mixin(LivingEntity.class)
public abstract class IncreaseMobAwarenessMixin extends Entity {
    public IncreaseMobAwarenessMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
        method = "getVisibilityPercent",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookGetVisibilityPercent(Entity entity, CallbackInfoReturnable<Double> cir) {
        if (entity instanceof Player player && CurseOfGlowing.playerHasCurse(player)) {
            var effect = new MobEffectInstance(MobEffects.GLOWING, 100);
            player.addEffect(effect);
            cir.setReturnValue(5.0D);
        }
    }
}
