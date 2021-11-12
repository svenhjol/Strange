package svenhjol.strange.module.runic_tomes.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface ActivateRunicTomeCallback {
    Event<ActivateRunicTomeCallback> EVENT = EventFactory.createArrayBacked(ActivateRunicTomeCallback.class, listeners -> (player, origin, tome, sacrifice) -> {
        for (ActivateRunicTomeCallback listener : listeners) {
            listener.interact(player, origin, tome, sacrifice);
        }
    });

    void interact(ServerPlayer player, BlockPos origin, ItemStack tome, ItemStack sacrifice);
}
