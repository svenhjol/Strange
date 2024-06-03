package svenhjol.strange.feature.runestones.common;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import svenhjol.charm.charmony.feature.FeatureHolder;
import svenhjol.strange.api.iface.RunestoneDefinition;
import svenhjol.strange.feature.runestones.Runestones;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class Handlers extends FeatureHolder<Runestones> {
    public final Map<Block, RunestoneDefinition> definitions = new HashMap<>();

    public Handlers(Runestones feature) {
        super(feature);
    }

    /**
     * Reload all the provider definitions into a map of runestone block -> runestone definition.
     */
    public void serverStart(MinecraftServer server) {
        definitions.clear();
        for (var definition : feature().providers.definitions) {
            this.definitions.put(definition.block().get(), definition);
        }
    }

    public void entityJoin(Entity entity, Level level) {
        if (entity instanceof ServerPlayer player) {
            var serverLevel = (ServerLevel)level;
            var seed = serverLevel.getSeed();
            Networking.S2CWorldSeed.send(player, seed);
            // TODO: clear cached runic names ??
        }
    }

    public void prepareRunestone(LevelAccessor level, BlockPos pos) {
        if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof RunestoneBlockEntity runestone)) {
            return;
        }

        var state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RunestoneBlock block)) {
            return;
        }

        if (!definitions.containsKey(block)) {
            log().error("No definition found for runestone block " + block + " at pos " + pos);
            return;
        }

        var random = RandomSource.create(pos.asLong());
        var definition = definitions.get(block);
        var location = definition.location(level, pos, random);

        if (location.isEmpty()) {
            log().error("Failed to get a location from runestone at pos " + pos);
            return;
        }

        runestone.location = location.get();
        log().debug("Set type " + runestone.location.id() + " for runestone at pos " + pos);
        runestone.setChanged();
    }
}
