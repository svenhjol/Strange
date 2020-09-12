package svenhjol.strange.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.WorldSaveHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import svenhjol.strange.event.PlayerLoadDataCallback;
import svenhjol.strange.event.PlayerSaveDataCallback;
import svenhjol.strange.mixin.accessor.WorldSaveHandlerAccessor;

import java.io.File;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Shadow @Final private WorldSaveHandler saveHandler;

    @Inject(
        method = "loadPlayerData",
        at = @At("HEAD")
    )
    private void hookLoadPlayerData(ServerPlayerEntity playerEntity, CallbackInfoReturnable<CompoundTag> cir) {
        PlayerLoadDataCallback.EVENT.invoker().interact(playerEntity, getPlayerDataDir());
    }

    @Inject(
        method = "savePlayerData",
        at = @At("RETURN")
    )
    private void hookSavePlayerData(ServerPlayerEntity playerEntity, CallbackInfo ci) {
        PlayerSaveDataCallback.EVENT.invoker().interact(playerEntity, getPlayerDataDir());
    }

    private File getPlayerDataDir() {
        return ((WorldSaveHandlerAccessor)this.saveHandler).getPlayerDataDir();
    }
}
