package svenhjol.strange.mixin.intercept_music;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import svenhjol.strange.Strange;
import svenhjol.strange.module.intercept_music.InterceptMusic;
import svenhjol.strange.module.intercept_music.InterceptMusicClient;

@Mixin(Minecraft.class)
public class TryCustomMusicMixin {
    @Inject(
        method = "getSituationalMusic",
        at = @At(value = "RETURN"),
        cancellable = true
    )
    private void hookSituationalMusic(CallbackInfoReturnable<Music> cir) {
        if (Strange.LOADER.isEnabled(InterceptMusic.class)) {
            Music music = InterceptMusicClient.replaceMusic(cir.getReturnValue());
            if (music != null) {
                cir.setReturnValue(music);
            }
        }
    }
}
