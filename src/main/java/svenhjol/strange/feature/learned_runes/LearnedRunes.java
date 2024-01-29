package svenhjol.strange.feature.learned_runes;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.strange.feature.learned_runes.LearnedRunesNetwork.SyncLearned;
import svenhjol.strange.feature.runestones.Location;
import svenhjol.strange.feature.travel_journal.TravelJournal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LearnedRunes extends CommonFeature {
    public static final String LEARNED_TAG = "learned";
    public static final Map<UUID, LearnedList> LEARNED = new HashMap<>();

    @Override
    public void register() {
        var registry = mod().registry();
        LearnedRunesNetwork.register(registry);

        TravelJournal.registerPlayerDataSource(
            (player, tag) -> LEARNED.put(player.getUUID(), LearnedList.load(tag.getCompound(LEARNED_TAG))),
            (player, tag) -> getLearned(player).ifPresent(learned -> tag.put(LEARNED_TAG, learned.save())));

        TravelJournal.registerSyncHandler(LearnedRunes::syncLearned);
    }

    public static void learn(ServerPlayer player, Location location) {
        getLearned(player).ifPresent(learned -> {
            learned.learn(location);
            SyncLearned.send(player, learned);
        });
    }

    public static Optional<LearnedList> getLearned(Player player) {
        return Optional.ofNullable(LEARNED.get(player.getUUID()));
    }

    public static void syncLearned(ServerPlayer player) {
        getLearned(player).ifPresent(
            learned -> SyncLearned.send(player, learned));
    }
}
