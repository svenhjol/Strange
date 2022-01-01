package svenhjol.strange.module.intercept_music;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@ClientModule(module = InterceptMusic.class)
public class InterceptMusicClient extends CharmModule {
    public static final List<MusicCondition> CONDITIONS = new LinkedList<>();

    public static void addCondition(MusicCondition condition) {
        CONDITIONS.add(condition);
        CONDITIONS.sort(Comparator.comparingInt(MusicCondition::priority).reversed());
    }

    public static void removeCondition(String id) {
        CONDITIONS.stream().filter(c -> c.id().equalsIgnoreCase(id)).findFirst().ifPresent(CONDITIONS::remove);
    }

    public static void stopMusic(boolean resetDelay) {
        var musicManager = Minecraft.getInstance().getMusicManager();
        musicManager.stopPlaying();

        if (resetDelay) {
            musicManager.nextSongDelay = 0;
        }
    }

    @Nullable
    public static Music replaceMusic(Music currentMusic) {
        for (MusicCondition c : CONDITIONS) {
            if (c.handle(currentMusic)) {
                return c.getMusic();
            }
        }

        return null;
    }
}
