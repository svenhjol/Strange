package svenhjol.strange.mixin.ambient_music_discs;

import com.mojang.blaze3d.audio.OggAudioStream;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import svenhjol.strange.feature.ambient_music_discs.AmbientMusicDiscs;
import svenhjol.strange.feature.ambient_music_discs.AmbientMusicDiscsClient;

import javax.sound.sampled.AudioFormat;
import java.io.InputStream;
import java.nio.IntBuffer;

@Mixin(OggAudioStream.class)
public class OggAudioStreamMixin {
    @Mutable
    @Shadow @Final private AudioFormat audioFormat;

    @Inject(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Ljavax/sound/sampled/AudioFormat;<init>(FIIZZ)V",
            shift = At.Shift.BY,
            by = 2
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void hookInit(InputStream inputStream, CallbackInfo ci, MemoryStack memoryStack, IntBuffer intBuffer, IntBuffer intBuffer2, STBVorbisInfo sTBVorbisInfo) {
        if (AmbientMusicDiscs.doExperimentalAttenuation && AmbientMusicDiscsClient.soundHolder != null) {
            var sampleRate = sTBVorbisInfo.sample_rate();
            if (sampleRate > 44100) {
                sampleRate = 44100; // 48k sounds are b0rk when doubled
            }
            this.audioFormat = new AudioFormat(
                sampleRate * 2, 16, 1, true, false
            );
            AmbientMusicDiscsClient.soundHolder = null;
        }
    }
}
