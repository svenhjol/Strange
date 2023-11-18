package svenhjol.strange.feature.cooking_pots;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import svenhjol.charmony.annotation.Packet;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.enums.PacketDirection;
import svenhjol.charmony.helper.PlayerHelper;
import svenhjol.charmony.iface.ICommonRegistry;
import svenhjol.charmony.iface.IPacketRequest;
import svenhjol.charmony.iface.IServerNetwork;
import svenhjol.strange.Strange;

public class CookingPotsNetwork {
    public static void register(ICommonRegistry registry) {
        registry.packet(new AddedToCookingPot(), () -> CookingPotsClient::handleAddedToCookingPot);
    }

    @Packet(
        id = "strange:added_to_cooking_pot",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Send the position of the cooking pot to the client."
    )
    public static class AddedToCookingPot implements IPacketRequest {
        private BlockPos pos;

        private AddedToCookingPot() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeBlockPos(pos);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            pos = buf.readBlockPos();
        }

        public BlockPos getPos() {
            return pos;
        }

        public static void send(Level level, BlockPos pos) {
            var message = new AddedToCookingPot();
            message.pos = pos;

            var players = PlayerHelper.getPlayersInRange(level, pos, 8.0d);
            players.forEach(player -> serverNetwork().send(message, player));
        }
    }

    static IServerNetwork serverNetwork() {
        return Mods.common(Strange.ID).network();
    }
}
