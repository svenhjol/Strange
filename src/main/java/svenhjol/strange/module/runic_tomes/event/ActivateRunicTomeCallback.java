package svenhjol.strange.module.runic_tomes.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ActivateRunicTomeCallback {
    Event<ActivateRunicTomeCallback> EVENT = EventFactory.createArrayBacked(ActivateRunicTomeCallback.class, listeners -> (player, tome, sacrifice) -> {
        for (ActivateRunicTomeCallback listener : listeners) {
            listener.interact(player, tome, sacrifice);
        }
    });

    void interact(Player player, ItemStack tome, ItemStack sacrifice);
}
