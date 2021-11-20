package svenhjol.strange.mixin.dimensions;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import svenhjol.strange.module.teleport.Teleport;

@Mixin(ServerLevel.class)
public class PreventEntityCreatingPlatformMixin {
    @Inject(
        method = "makeObsidianPlatform",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void hookMakeObsidianPlatform(ServerLevel serverLevel, CallbackInfo ci) {
        Entity entity = Teleport.entityCreatingPlatform.get();
        if (entity != null && Teleport.noEndPlatform.contains(entity.getUUID())) {
            Teleport.entityCreatingPlatform.remove();
            Teleport.noEndPlatform.clear();
            ci.cancel();
        }
    }
}
