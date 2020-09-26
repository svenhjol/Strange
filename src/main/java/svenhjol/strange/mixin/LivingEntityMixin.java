package svenhjol.strange.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import svenhjol.strange.event.EntityDeathCallback;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(
        method = "onDeath",
        at = @At("HEAD")
    )
    private void hookOnDeath(DamageSource source, CallbackInfo ci) {
        EntityDeathCallback.EVENT.invoker().interact((LivingEntity)(Object)this, source);
    }
}
