package svenhjol.strange.mixin.teleport;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import svenhjol.strange.module.teleport.Teleport;

@Mixin(Entity.class)
public class CaptureEntityCreatingPlatformMixin {
    @Inject(
        method = "changeDimension",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;makeObsidianPlatform(Lnet/minecraft/server/level/ServerLevel;)V",
            shift = At.Shift.BEFORE
        )
    )
    private void hookBeforeMakePlatform(ServerLevel serverLevel, CallbackInfoReturnable<Entity> cir) {
        Teleport.entityCreatingPlatform.set((Entity)(Object)this);
    }
}
