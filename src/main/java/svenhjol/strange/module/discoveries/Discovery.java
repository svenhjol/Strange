package svenhjol.strange.module.discoveries;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.module.runes.RuneHelper;

import javax.annotation.Nullable;
import java.util.UUID;

public class Discovery {
    public static final String RUNES_TAG = "Runes";
    public static final String LOCATION_TAG = "Location";
    public static final String POS_TAG = "Pos";
    public static final String DIM_TAG = "Dim";
    public static final String PLAYER_TAG = "Player";
    public static final String DIFFICULTY_TAG = "Difficulty";
    public static final String TIME_TAG = "Time";

    private final String runes;
    private final ResourceLocation location;
    private BlockPos pos;
    private ResourceLocation dimension;
    private UUID player;
    private float difficulty;
    private long time;

    private long cachedSeed;

    public Discovery(String runes, ResourceLocation location) {
        this.runes = runes;
        this.location = location;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putFloat(DIFFICULTY_TAG, difficulty);

        if (runes != null) {
            tag.putString(RUNES_TAG, runes);
        }

        if (pos != null) {
            tag.putLong(POS_TAG, pos.asLong());
        }

        if (dimension != null && !dimension.toString().isEmpty()) {
            tag.putString(DIM_TAG, dimension.toString());
        }

        if (location != null) {
            tag.putString(LOCATION_TAG, location.toString());
        }

        if (player != null) {
            tag.putUUID(PLAYER_TAG, player);
        }

        tag.putLong(TIME_TAG, time);

        return tag;
    }

    public void setDimension(ResourceLocation dimension) {
        this.dimension = dimension;
    }

    public void setDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }

    public void setPlayer(UUID player) {
        this.player = player;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getRunes() {
        return this.runes;
    }

    public long getSeed() {
        if (cachedSeed == 0) {
            cachedSeed = RuneHelper.seed(runes);
            LogHelper.debug(Strange.MOD_ID, this.getClass(), "This block's random seed is " + cachedSeed);
        }
        return cachedSeed;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public float getDifficulty() {
        return difficulty;
    }

    public UUID getPlayer() {
        return player;
    }

    public long getTime() {
        return time;
    }

    @Nullable
    public BlockPos getPos() {
        return pos;
    }

    @Nullable
    public ResourceLocation getDimension() {
        return dimension;
    }

    public static Discovery load(CompoundTag tag) {
        String runes = tag.getString(RUNES_TAG);

        ResourceLocation location = new ResourceLocation(tag.getString(LOCATION_TAG));
        Discovery discovery = new Discovery(runes, location);

        String dimensionFromTag = tag.getString(DIM_TAG);
        discovery.dimension = dimensionFromTag.isEmpty() ? null : new ResourceLocation(dimensionFromTag);

        BlockPos pos = BlockPos.of(tag.getLong(POS_TAG));
        discovery.pos = pos.equals(BlockPos.ZERO) ? null : pos;

        String playerFromTag = tag.getString(PLAYER_TAG);
        discovery.player = playerFromTag.isEmpty() ? null : tag.getUUID(PLAYER_TAG);

        discovery.difficulty = tag.getFloat(DIFFICULTY_TAG);

        discovery.time = tag.getLong(TIME_TAG);
        return discovery;
    }
}
