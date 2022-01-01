package svenhjol.strange.module.intercept_music;

import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Predicate;

public record MusicCondition(String id, int priority, SoundEvent sound, int minDelay, int maxDelay, boolean replace, Predicate<Music> condition) {
    public boolean handle(Music currentMusic) {
        if (condition == null) return false;
        return condition.test(currentMusic);
    }

    public Music getMusic() {
        return new Music(sound, minDelay, maxDelay, replace);
    }
}
