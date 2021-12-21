package svenhjol.strange.module.teleport.runic;

import svenhjol.strange.module.teleport.iface.ITeleportType;
import svenhjol.strange.module.teleport.runic.network.ClientRunicTeleportEffectReceiver;

public class RunicTeleportClient implements ITeleportType {
    public static ClientRunicTeleportEffectReceiver RUNIC_TELEPORT_EFFECT_RECEIVER;

    @Override
    public void register() {}

    @Override
    public void runWhenEnabled() {
        RUNIC_TELEPORT_EFFECT_RECEIVER = new ClientRunicTeleportEffectReceiver(RunicTeleport.RUNIC_TELEPORT_EFFECT_SENDER.id());
//        ClientPlayNetworking.registerGlobalReceiver(RunicTeleportClientMessages.CLIENT_RUNIC_TELEPORT_EFFECT, this::handleRunicTeleportEffect);
    }
}
