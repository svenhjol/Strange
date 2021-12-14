package svenhjol.strange.module.dimensions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IDimension {
    ResourceLocation getId();

    void register();

    void handleWorldLoad(MinecraftServer server, ServerLevel level);

    void handleWorldTick(Level level);

    InteractionResult handleAddEntity(Entity entity);

    void handlePlayerTick(Player player);

    default void removestructures(ServerLevel level, List<StructureFeature<?>> structureFeatures) {
        StructureSettings settings = level.getChunkSource().getGenerator().getSettings();
        Map<StructureFeature<?>, StructureFeatureConfiguration> structureConfig = new HashMap<>(settings.structureConfig());

        for (StructureFeature<?> structureFeature : structureFeatures) {
            structureConfig.remove(structureFeature);
        }

        settings.structureConfig = structureConfig;
    }
}
