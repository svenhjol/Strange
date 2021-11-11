package svenhjol.strange.module.runestones.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ActivateRunestoneCallback {
    Event<ActivateRunestoneCallback> EVENT = EventFactory.createArrayBacked(ActivateRunestoneCallback.class, listeners -> (player, runes, sacrifice) -> {
        for (ActivateRunestoneCallback listener : listeners) {
            listener.interact(player, runes, sacrifice);
        }
    });

    void interact(Player player, String runes, ItemStack sacrifice);
}
