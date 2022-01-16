package svenhjol.strange.mixin.curse_of_detecting;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import svenhjol.strange.module.curse_of_detecting.CurseOfDetecting;

import java.util.Map;
import java.util.WeakHashMap;

@Mixin(TargetingConditions.class)
public class IgnoreLineOfSightMixin {
    @Shadow private boolean checkLineOfSight;

    private final ThreadLocal<LivingEntity> target = new ThreadLocal<>();
    private final Map<LivingEntity, Boolean> shouldCheckLineOfSight = new WeakHashMap<>();

    @Inject(
        method = "test",
        at = @At("HEAD")
    )
    private void hookIgnoreLineOfSight(LivingEntity source, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (CurseOfDetecting.hasCurse(target)) {
            shouldCheckLineOfSight.put(target, checkLineOfSight);
            checkLineOfSight = false;

            this.target.set(target);
        } else {
            if (shouldCheckLineOfSight.containsKey(target)) {
                checkLineOfSight = shouldCheckLineOfSight.get(target);
            }
            shouldCheckLineOfSight.remove(target);
            this.target.remove();
        }
    }

    @Redirect(
        method = "test",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Math;max(DD)D"
        )
    )
    private double hookRemoveDistanceLimit(double a, double b) {
        var target = this.target.get();
        if (target != null) {
            return a;
        }

        return Math.max(a, b);
    }
}
