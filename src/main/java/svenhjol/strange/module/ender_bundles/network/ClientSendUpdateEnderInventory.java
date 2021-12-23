package svenhjol.strange.module.ender_bundles.network;

import svenhjol.charm.network.ClientSender;
import svenhjol.charm.network.Id;

/**
 * Client sends empty packet to request an updated ender inventory.
 */
@Id("strange:update_ender_inventory")
public class ClientSendUpdateEnderInventory extends ClientSender {
    @Override
    protected boolean showDebugMessages() {
        return false;
    }
}
