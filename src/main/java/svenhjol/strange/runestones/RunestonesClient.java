package svenhjol.strange.runestones;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;

import java.util.HashMap;
import java.util.Map;

public class RunestonesClient extends CharmClientModule {
    public static Map<Integer, String> DESTINATION_NAMES = new HashMap<>();

    public RunestonesClient(CharmModule module) {
        super(module);
    }

    @Override
    public void register() {
        // listen for player runestone discoveries being sent from the server
        ClientSidePacketRegistry.INSTANCE.register(Runestones.MSG_CLIENT_SYNC_LEARNED, this::handleClientSyncLearned);

        ClientSidePacketRegistry.INSTANCE.register(Runestones.MSG_CLIENT_SYNC_DESTINATION_NAMES, this::handleClientSyncDestinationNames);
    }

    private void handleClientSyncLearned(PacketContext context, PacketByteBuf data) {
        int[] discoveries = data.readIntArray();
        context.getTaskQueue().execute(() -> {
            RunestonesHelper.populateLearnedRunes(context.getPlayer(), discoveries);
        });
    }

    private void handleClientSyncDestinationNames(PacketContext context, PacketByteBuf data) {
        CompoundTag inTag = data.readCompoundTag();
        if (inTag == null || inTag.isEmpty())
            return;

        context.getTaskQueue().execute(() -> {
            DESTINATION_NAMES.clear();

            for (int i = 0; i < RunestonesHelper.NUMBER_OF_RUNES; i++) {
                String name = inTag.getString(String.valueOf(i));
                if (!name.isEmpty())
                    DESTINATION_NAMES.put(i, name);
            }
        });
    }
}
