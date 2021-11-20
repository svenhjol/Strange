package svenhjol.strange.module.dimensions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface IDimension {
    ResourceLocation getId();

    void register();

    void handleWorldTick(Level level);

    void handleAddEntity(Entity entity);

    void handlePlayerTick(Player player);
}
