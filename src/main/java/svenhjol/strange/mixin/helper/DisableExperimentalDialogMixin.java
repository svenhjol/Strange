package svenhjol.strange.mixin.helper;

import com.mojang.datafixers.util.Function4;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(Minecraft.class)
public abstract class DisableExperimentalDialogMixin {
    @Shadow protected abstract void doLoadLevel(String string, RegistryAccess.RegistryHolder registryHolder, Function<LevelStorageSource.LevelStorageAccess, DataPackConfig> function, Function4<LevelStorageSource.LevelStorageAccess, RegistryAccess.RegistryHolder, ResourceManager, DataPackConfig, WorldData> function4, boolean bl, Minecraft.ExperimentalDialogType experimentalDialogType);

    private boolean skippedDialog;

    @Redirect(
        method = "doLoadLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;displayExperimentalConfirmationDialog(Lnet/minecraft/client/Minecraft$ExperimentalDialogType;Ljava/lang/String;ZLjava/lang/Runnable;)V"
        )
    )
    private void hookDisplayDialog(Minecraft minecraft, Minecraft.ExperimentalDialogType dialog, String string, boolean bl, Runnable runnable) {
        // no op
        skippedDialog = true;
    }

    @Inject(
        method = "doLoadLevel",
        at = @At("RETURN")
    )
    private void hookForceLoadLevel(String string, RegistryAccess.RegistryHolder registryHolder, Function<LevelStorageSource.LevelStorageAccess, DataPackConfig> function, Function4<LevelStorageSource.LevelStorageAccess, RegistryAccess.RegistryHolder, ResourceManager, DataPackConfig, WorldData> function4, boolean bl, Minecraft.ExperimentalDialogType experimentalDialogType, CallbackInfo ci) {
        if (skippedDialog) {
            skippedDialog = false;
            doLoadLevel(string, registryHolder, function, function4, bl, Minecraft.ExperimentalDialogType.NONE);
        }
    }
}
