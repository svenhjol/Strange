package svenhjol.strange.mixin.feature.runestones;

import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import svenhjol.charm.charmony.Resolve;
import svenhjol.strange.feature.runestones.Runestones;

@Mixin(ThrownEnderpearl.class)
public class ThrownEnderpearlMixin {
    @Inject(
        method = "onHit",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookOnHit(HitResult hitResult, CallbackInfo ci) {
        var result = Resolve.feature(Runestones.class).handlers.enderpearlImpact((ThrownEnderpearl)(Object)this, hitResult);
        if (result) {
            ci.cancel();
        }
    }
}
