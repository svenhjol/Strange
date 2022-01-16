package svenhjol.strange.mixin.curse_of_detecting;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import svenhjol.strange.module.curse_of_detecting.CurseOfDetecting;

@Mixin(LivingEntity.class)
public abstract class IncreaseMobAwarenessMixin {
    @Inject(
        method = "getVisibilityPercent",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookGetVisibilityPercent(Entity entity, CallbackInfoReturnable<Double> cir) {
        var entity1 = (Entity) (Object) this;
        if (entity1 instanceof LivingEntity livingEntity && CurseOfDetecting.hasCurse(livingEntity)) {
            cir.setReturnValue(CurseOfDetecting.detectionRange);
        }
    }
}
