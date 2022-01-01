package svenhjol.strange.module.vaults;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.api.event.ClientStateUpdateCallback;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.module.player_state.PlayerState;
import svenhjol.strange.Strange;
import svenhjol.strange.module.intercept_music.InterceptMusic;
import svenhjol.strange.module.intercept_music.InterceptMusicClient;
import svenhjol.strange.module.intercept_music.MusicCondition;

@ClientModule(module = Vaults.class)
public class VaultsClient extends CharmModule {
    private boolean inVaults = false;

    @Override
    public void register() {
        ClientStateUpdateCallback.EVENT.register(this::handleCharmState);

        if (Strange.LOADER.isEnabled(InterceptMusic.class)) {
            var ambientMusicCondition = new MusicCondition("vaults_music", 10, Vaults.VAULTS_MUSIC, 12000, 24000, true, current -> {
                var mc = Minecraft.getInstance();
                if (mc == null || mc.player == null) return false;
                return inVaults;
            });

            InterceptMusicClient.addCondition(ambientMusicCondition);
        }
    }

    private void handleCharmState(CompoundTag tag) {
        if (tag.contains(PlayerState.WITHIN_STRUCTURES_TAG)) {
            boolean nowInVaults = tag.getList(PlayerState.WITHIN_STRUCTURES_TAG, 8).stream()
                .map(Tag::getAsString)
                .anyMatch(s -> s.equalsIgnoreCase(Vaults.STRUCTURE_ID.toString()));

            if (!this.inVaults && nowInVaults) {
                InterceptMusicClient.stopMusic(true);
            }

            this.inVaults = nowInVaults;
        }
    }
}
