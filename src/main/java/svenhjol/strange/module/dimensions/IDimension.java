package svenhjol.strange.module.dimensions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public interface IDimension {
    ResourceLocation getId();

    void register();

    default void handleWorldLoad(MinecraftServer server, ServerLevel level) {
        // override me
    }

    default void handleWorldTick(Level level) {
        // override me
    }

    @Nullable
    default InteractionResult handleAddEntity(Entity entity) {
        // override me
        return null;
    }

    default void handlePlayerChangeDimension(ServerPlayer player, ServerLevel origin, ServerLevel destination) {
        // override me
    }

    default void handlePlayerTick(Player player) {
        // override me
    }
}
