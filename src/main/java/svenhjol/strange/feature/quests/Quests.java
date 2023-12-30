package svenhjol.strange.feature.quests;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import svenhjol.charmony.api.event.EntityJoinEvent;
import svenhjol.charmony.api.event.ServerStartEvent;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;

public class Quests extends CommonFeature {
    static final List<IQuestDefinition> DEFINITIONS = new ArrayList<>();

    @Override
    public void register() {
        QuestDefinitions.init();
    }

    @Override
    public void runWhenEnabled() {
        ServerStartEvent.INSTANCE.handle(this::handleServerStart);
        EntityJoinEvent.INSTANCE.handle(this::handleEntityJoin);
    }

    public static void registerDefinition(IQuestDefinition definition) {
        Mods.common(Strange.ID).log().debug(Quests.class, "Registering definition " + definition);
        DEFINITIONS.add(definition);
    }

    private void handleServerStart(MinecraftServer server) {

    }

    private void handleEntityJoin(Entity entity, Level level) {
        if (entity instanceof ServerPlayer player) {
            // TODO: sync initial stuff
        }
    }
}
