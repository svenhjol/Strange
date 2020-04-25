package svenhjol.strange.base;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import svenhjol.charm.base.message.ServerUpdatePlayerState;
import svenhjol.meson.Meson;
import svenhjol.strange.ruins.module.UndergroundRuins;
import svenhjol.strange.ruins.module.Vaults;
import svenhjol.strange.runestones.capability.IRunestonesCapability;
import svenhjol.strange.runestones.module.Runestones;

public class StrangeServer {
    public StrangeServer() {
        ServerUpdatePlayerState.runOnUpdate.add(this::updateDiscoveredRunes);
        ServerUpdatePlayerState.runOnUpdate.add(this::updateStructures);
    }

    public void updateDiscoveredRunes(Context context, CompoundNBT nbt) {
        ServerPlayerEntity player = context.getSender();
        if (player == null) return;

        if (Meson.isModuleEnabled("strange:runestones")) {
            IRunestonesCapability runeCap = Runestones.getCapability(player);
            nbt.putIntArray("discoveredRunes", runeCap.getDiscoveredTypes());
        }
    }

    public void updateStructures(Context context, CompoundNBT nbt) {
        ServerPlayerEntity player = context.getSender();
        if (player == null) return;

        ServerWorld world = player.getServerWorld();
        BlockPos pos = player.getPosition();

        if (Meson.isModuleEnabled("strange:underground_ruins")) {
            nbt.putBoolean("underground_ruin", UndergroundRuins.structure.isPositionInsideStructure(world, pos));
        }
        if (Meson.isModuleEnabled("strange:vaults")) {
            nbt.putBoolean("vaults", Vaults.structure.isPositionInsideStructure(world, pos));
        }
    }
}
