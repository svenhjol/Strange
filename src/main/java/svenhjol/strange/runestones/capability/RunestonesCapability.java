package svenhjol.strange.runestones.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunestonesCapability implements IRunestonesCapability {
    private final List<Integer> discoveredTypes = new ArrayList<>();
    private final Map<Long, Long> destinations = new HashMap<>();

    @Override
    public void discoverType(int rune) {
        if (!discoveredTypes.contains(rune)) {
            discoveredTypes.add(rune);
        }
    }

    @Override
    public void recordDestination(BlockPos runePos, BlockPos destPos) {
        long r = runePos.toLong();
        long d = destPos.toLong();

        if (!destinations.containsKey(r)) {
            destinations.put(r, d);
        }
    }

    @Override
    public List<Integer> getDiscoveredTypes() {
        return discoveredTypes;
    }

    @Nullable
    @Override
    public BlockPos getDestination(BlockPos runePos) {
        long r = runePos.toLong();
        if (destinations.containsKey(r)) {
            return BlockPos.fromLong(destinations.get(r));
        }
        return null;
    }

    @Override
    public void readNBT(INBT tag) {
        if (tag == null) tag = new CompoundNBT();
        CompoundNBT nbt = (CompoundNBT) tag;

        if (!nbt.contains("destinations")) {
            nbt.put("destinations", new CompoundNBT());
        }

        // get dests as a compound tag of key => value pairs and convert them to longs
        CompoundNBT dests = nbt.getCompound("destinations");

        destinations.clear();
        for (String left : dests.keySet()) {
            Long runePos = Long.valueOf(left);
            Long destPos = dests.getLong(left);
            destinations.put(runePos, destPos);
        }

        // get types as an array of integers from the saved data
        int[] types = nbt.getIntArray("discoveredTypes");
        discoveredTypes.clear();

        for (int type : types) {
            discoveredTypes.add(type);
        }
    }

    @Override
    public INBT writeNBT() {
        CompoundNBT tag = new CompoundNBT();

        CompoundNBT dests = new CompoundNBT();
        for (Long runePos : destinations.keySet()) {
            dests.putLong(String.valueOf(runePos), destinations.get(runePos));
        }
        tag.put("destinations", dests);
        tag.putIntArray("discoveredTypes", discoveredTypes);

        return tag;
    }
}
