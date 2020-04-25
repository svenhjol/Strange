package svenhjol.strange.base;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import svenhjol.charm.base.message.ClientUpdatePlayerState;
import svenhjol.meson.Meson;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class StrangeClient {
    public StrangeClient() {
        ClientUpdatePlayerState.runOnUpdate.add(this::updateDiscoveries);
    }

    public List<Integer> discoveredRunes = new ArrayList<>();

    public void updateDiscoveries(CompoundNBT input) {
        if (Meson.isModuleEnabled("strange:runestones") && input.contains("discoveredRunes")) {
            discoveredRunes = new ArrayList<>();
            int[] ii = input.getIntArray("discoveredRunes");
            for (int i : ii) {
                discoveredRunes.add(i);
            }
        }
    }
}
