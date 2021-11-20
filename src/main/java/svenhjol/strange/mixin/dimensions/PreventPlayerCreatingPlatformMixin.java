package svenhjol.strange.mixin.dimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import svenhjol.strange.module.teleport.Teleport;

import java.util.UUID;

@Mixin(ServerPlayer.class)
public class PreventPlayerCreatingPlatformMixin {
    @Inject(
        method = "createEndPlatform",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookCreateEndPlatform(ServerLevel serverLevel, BlockPos blockPos, CallbackInfo ci) {
        ServerPlayer serverPlayer = (ServerPlayer) (Object) this;
        UUID uuid = serverPlayer.getUUID();
        if (Teleport.noEndPlatform.contains(uuid)) {
            Teleport.noEndPlatform.remove(uuid);
            ci.cancel();
        }
    }
}
