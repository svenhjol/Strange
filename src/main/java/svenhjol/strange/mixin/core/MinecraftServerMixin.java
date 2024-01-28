package svenhjol.strange.mixin.core;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import svenhjol.strange.data.ResourceListManager;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    /**
     * Clears the custom resource list entries cache when server packs are reloaded.
     */
    @Inject(
        method = "reloadResources",
        at = @At("HEAD")
    )
    private void hookReloadResources(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        ResourceListManager.clearEntriesCache();
    }
}
