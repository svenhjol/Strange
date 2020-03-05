package svenhjol.strange.base.message;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.Strange;
import svenhjol.strange.ruins.module.UndergroundRuins;
import svenhjol.strange.ruins.module.Vaults;
import svenhjol.strange.runestones.capability.IRunestonesCapability;
import svenhjol.strange.runestones.module.Runestones;

import java.util.function.Supplier;

/**
 * Server assembles list of state for client, like inside structures, is day or night...
 */
public class ServerUpdatePlayerState implements IMesonMessage
{
    public ServerUpdatePlayerState()
    {
    }

    public static void encode(ServerUpdatePlayerState msg, PacketBuffer buf)
    {
    }

    public static ServerUpdatePlayerState decode(PacketBuffer buf)
    {
        return new ServerUpdatePlayerState();
    }

    public static class Handler
    {
        public static void handle(final ServerUpdatePlayerState msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                NetworkEvent.Context context = ctx.get();
                ServerPlayerEntity player = context.getSender();
                if (player == null) return;

                ServerWorld world = player.getServerWorld();
                BlockPos pos = player.getPosition();

                CompoundNBT nbt = new CompoundNBT();
                nbt.putBoolean("mineshaft", Feature.MINESHAFT.isPositionInsideStructure(world, pos));
                nbt.putBoolean("stronghold", Feature.STRONGHOLD.isPositionInsideStructure(world, pos));
                nbt.putBoolean("fortress", Feature.NETHER_BRIDGE.isPositionInsideStructure(world, pos));
                nbt.putBoolean("shipwreck", Feature.SHIPWRECK.isPositionInsideStructure(world, pos));
                nbt.putBoolean("village", Feature.VILLAGE.isPositionInsideStructure(world, pos));
                nbt.putBoolean("day", world.getDayTime() > 0 && world.getDayTime() < 12700);

                if (Meson.isModuleEnabled("strange:runestones")) {
                    IRunestonesCapability runeCap = Runestones.getCapability(player);
                    nbt.putIntArray("discoveredRunes", runeCap.getDiscoveredTypes());
                }

                if (Meson.isModuleEnabled("strange:underground_ruins")) {
                    nbt.putBoolean("underground_ruin", UndergroundRuins.structure.isPositionInsideStructure(world, pos));
                }
                if (Meson.isModuleEnabled("strange:vaults")) {
                    nbt.putBoolean("vaults", Vaults.structure.isPositionInsideStructure(world, pos));
                }
                if (Strange.quarkCompat != null && Strange.quarkCompat.hasModule(new ResourceLocation("quark:big_dungeons"))) {
                    nbt.putBoolean("big_dungeon", Strange.quarkCompat.isInsideBigDungeon(world, pos));
                }

                PacketHandler.sendTo(new ClientUpdatePlayerState(nbt), player);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
