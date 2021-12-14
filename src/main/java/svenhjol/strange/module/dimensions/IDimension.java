package svenhjol.strange.module.dimensions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface IDimension {
    ResourceLocation getId();

    void register();

    void handleWorldLoad(MinecraftServer server, ServerLevel level);

    void handleWorldTick(Level level);

    void handleAddEntity(Entity entity);

    void handlePlayerTick(Player player);
}
