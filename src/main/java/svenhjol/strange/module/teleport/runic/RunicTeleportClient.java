package svenhjol.strange.module.teleport.runic;

import svenhjol.strange.module.teleport.iface.ITeleportType;
import svenhjol.strange.module.teleport.runic.network.ClientReceiveRunicTeleportEffect;

public class RunicTeleportClient implements ITeleportType {
    public static ClientReceiveRunicTeleportEffect RECEIVE_RUNIC_TELEPORT_EFFECT;

    @Override
    public void register() {}

    @Override
    public void runWhenEnabled() {
        RECEIVE_RUNIC_TELEPORT_EFFECT = new ClientReceiveRunicTeleportEffect();
//        ClientPlayNetworking.registerGlobalReceiver(RunicTeleportClientMessages.CLIENT_RUNIC_TELEPORT_EFFECT, this::handleRunicTeleportEffect);
    }
}
