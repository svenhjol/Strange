package svenhjol.strange.module.knowledge;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

import java.util.Optional;

@CommonModule(mod = Strange.MOD_ID)
public class Knowledge extends CharmModule {
    public static final int NUM_RUNES = 26;
    public static long seed;
    public static KnowledgeData savedData;

    @Override
    public void register() {
        CommandRegistrationCallback.EVENT.register(this::handleRegisterCommand);
    }

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        ServerEntityEvents.ENTITY_LOAD.register(this::handleEntityLoad);
    }

    public static Optional<KnowledgeData> getSavedData() {
        return Optional.ofNullable(savedData);
    }

    private void handleWorldLoad(MinecraftServer server, Level level) {
        if (level.dimension() == Level.OVERWORLD) {
            // overworld gets loaded first, set up the knowledge data here
            ServerLevel overworld = (ServerLevel)level;

            seed = overworld.getSeed();
            DimensionDataStorage storage = overworld.getDataStorage();

            savedData = storage.computeIfAbsent(
                nbt -> KnowledgeData.fromNbt(overworld, nbt),
                () -> new KnowledgeData(overworld),
                KnowledgeData.getFileId(overworld.getLevel().dimensionType()));

            savedData.populate(server);
            LogHelper.info(this.getClass(), "Loaded knowledge saved data");

        } else {
            getSavedData().ifPresent(data -> {
                // capture the world that just got loaded
                data.registerDimension(level);
                data.setDirty();
            });
        }
    }

    private void handleEntityLoad(Entity entity, ServerLevel level) {
        if (entity instanceof Player) {
            getSavedData().ifPresent((data) -> {
                data.registerPlayer((Player)entity);
                data.setDirty();
            });
        }
    }

    private void handleRegisterCommand(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        KnowledgeCommand.register(dispatcher);
    }
}
