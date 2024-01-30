package svenhjol.strange.feature.learned_runes;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.strange.feature.learned_runes.LearnedRunesNetwork.SyncLearned;
import svenhjol.strange.feature.runestones.Location;
import svenhjol.strange.feature.travel_journal.TravelJournal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LearnedRunes extends CommonFeature {
    public static final String LEARNED_TAG = "learned";
    public static final Map<UUID, LearnedList> LEARNED = new HashMap<>();

    @Override
    public void register() {
        var registry = mod().registry();
        LearnedRunesNetwork.register(registry);
    }

    @Override
    public void runWhenEnabled() {
        TravelJournal.registerPlayerDataSource(
            (player, tag) -> LEARNED.put(player.getUUID(), LearnedList.load(tag.getCompound(LEARNED_TAG))),
            (player, tag) -> tag.put(LEARNED_TAG, getLearned(player).save()));

        TravelJournal.registerSyncHandler(LearnedRunes::syncLearned);
    }

    public static void learn(ServerPlayer player, Location location) {
        var learned = getLearned(player);
        learned.learn(location);
        SyncLearned.send(player, learned);
    }

    public static LearnedList getLearned(Player player) {
        return LEARNED.getOrDefault(player.getUUID(), new LearnedList());
    }

    public static void syncLearned(ServerPlayer player) {
        SyncLearned.send(player, getLearned(player));
    }
}
