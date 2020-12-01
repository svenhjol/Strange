package svenhjol.strange.runestones;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import svenhjol.strange.runestones.destination.Destination;

import java.util.List;

public class RunestonesServer {
    public void init() {

    }

    public static void sendLearnedRunesPacket(ServerPlayerEntity player) {
        List<Integer> learnedRunes = RunestonesHelper.getLearnedRunes(player);
        int[] learned = learnedRunes.stream().mapToInt(i -> i).toArray();

        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
        data.writeIntArray(learned);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Runestones.MSG_CLIENT_CACHE_LEARNED_RUNES, data);
    }

    public static void sendDestinationNamesPacket(ServerPlayerEntity player) {
        List<Integer> learnedRunes = RunestonesHelper.getLearnedRunes(player);

        CompoundTag outTag = new CompoundTag();

        for (int rune : learnedRunes) {
            Destination destination = Runestones.WORLD_DESTINATIONS.get(rune);
            String name = RunestonesHelper.getFormattedLocationName(destination.getLocation());
            outTag.putString(String.valueOf(rune), name);
        }

        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
        data.writeCompoundTag(outTag);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Runestones.MSG_CLIENT_CACHE_DESTINATION_NAMES, data);
    }
}
