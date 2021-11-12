package svenhjol.strange.module.runestones.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface ActivateRunestoneCallback {
    Event<ActivateRunestoneCallback> EVENT = EventFactory.createArrayBacked(ActivateRunestoneCallback.class, listeners -> (player, origin, runes, sacrifice) -> {
        for (ActivateRunestoneCallback listener : listeners) {
            listener.interact(player, origin, runes, sacrifice);
        }
    });

    void interact(ServerPlayer player, BlockPos origin, String runes, ItemStack sacrifice);
}
