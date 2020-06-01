package svenhjol.strange.traveljournal.storage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.Map;

public class TravelJournalSavedData extends WorldSavedData {
    private static final String ID = "TravelJournalSavedData";
    private static final String POSITIONS = "positions";
    private static final String DIMENSIONS = "dimensions";
    public Map<String, BlockPos> positions = new HashMap<>();
    public Map<String, Integer> dimensions = new HashMap<>();

    public TravelJournalSavedData() {
        super(ID);
    }

    @Override
    public void read(CompoundNBT tag) {
        // get dimensions
        for (String key : getTagDimensions(tag).keySet()) {
            dimensions.put(key, getTagDimensions(tag).getInt(key));
        }

        // get positions
        for (String key : getTagPositions(tag).keySet()) {
            positions.put(key, BlockPos.fromLong(getTagPositions(tag).getLong(key)));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        // set dimensions
        for (String key : dimensions.keySet()) {
            Integer dimensionId = dimensions.get(key);
            getTagDimensions(tag).putInt(key, dimensionId);
        }

        // set positions
        for (String key : positions.keySet()) {
            BlockPos pos = positions.get(key);
            getTagPositions(tag).putLong(key, pos.toLong());
        }

        return tag;
    }

    public static TravelJournalSavedData get(ServerWorld world) {
        return world.getSavedData().getOrCreate(TravelJournalSavedData::new, ID);
    }

    private CompoundNBT getTagPositions(CompoundNBT tag) {
        CompoundNBT p = (CompoundNBT) tag.get(POSITIONS);
        if (p == null || p.isEmpty()) {
            tag.put(POSITIONS, new CompoundNBT());
        }
        return (CompoundNBT) tag.get(POSITIONS);
    }

    private CompoundNBT getTagDimensions(CompoundNBT tag) {
        CompoundNBT l = (CompoundNBT) tag.get(DIMENSIONS);
        if (l == null || l.isEmpty()) {
            tag.put(DIMENSIONS, new CompoundNBT());
        }
        return (CompoundNBT) tag.get(DIMENSIONS);
    }
}
