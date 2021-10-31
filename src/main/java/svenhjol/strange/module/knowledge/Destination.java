package svenhjol.strange.module.knowledge;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LogHelper;

import java.util.Optional;

public class Destination {
    public static final String TAG_RUNES = "Runes";
    public static final String TAG_POSITION = "Pos";
    public static final String TAG_DIMENSION = "Dim";
    public static final String TAG_LOCATION = "Loc";
    public static final String TAG_PLAYER = "Player";
    public static final String TAG_DIFFICULTY = "Diff";
    public static final String TAG_DECAY = "Decay";

    private final String runes;
    private final ResourceLocation location;
    private BlockPos pos;
    private ResourceLocation dimension;
    private String player;
    private float difficulty;
    private float decay;

    private long cachedSeed;

    public Destination(String runes, ResourceLocation location) {
        this.runes = runes;
        this.location = location;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putFloat(TAG_DIFFICULTY, difficulty);
        tag.putFloat(TAG_DECAY, decay);

        if (runes != null) {
            tag.putString(TAG_RUNES, runes);
        }

        if (pos != null) {
            tag.putLong(TAG_POSITION, pos.asLong());
        }

        if (dimension != null && !dimension.toString().isEmpty()) {
            tag.putString(TAG_DIMENSION, dimension.toString());
        }

        if (location != null) {
            tag.putString(TAG_LOCATION, location.toString());
        }

        if (player != null) {
            tag.putString(TAG_PLAYER, player);
        }

        return tag;
    }

    public void setDecay(float decay) {
        this.decay = decay;
    }

    public void setDimension(ResourceLocation dimension) {
        this.dimension = dimension;
    }

    public void setDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public String getRunes() {
        return this.runes;
    }

    public long getSeed() {
        if (cachedSeed == 0) {
            cachedSeed = KnowledgeHelper.generateSeedFromString(this.runes);
            LogHelper.debug(this.getClass(), "This block's random seed is " + cachedSeed);
        }
        return cachedSeed;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public float getDifficulty() {
        return difficulty;
    }

    public float getDecay() {
        return decay;
    }

    public String getPlayer() {
        return player;
    }

    public Optional<BlockPos> getPos() {
        return Optional.ofNullable(pos);
    }

    public Optional<ResourceLocation> getDimension() {
        return Optional.ofNullable(dimension);
    }

    public static Destination fromTag(CompoundTag tag) {
        String runes = tag.getString(TAG_RUNES);
        ResourceLocation location = new ResourceLocation(tag.getString(TAG_LOCATION));
        Destination destination = new Destination(runes, location);

        String dimensionFromTag = tag.getString(TAG_DIMENSION);
        destination.dimension = dimensionFromTag.isEmpty() ? null : new ResourceLocation(dimensionFromTag);

        BlockPos pos = BlockPos.of(tag.getLong(TAG_POSITION));
        destination.pos = pos.equals(BlockPos.ZERO) ? null : pos;

        String playerFromTag = tag.getString(TAG_PLAYER);
        destination.player = playerFromTag.isEmpty() ? null : tag.getString(TAG_PLAYER);

        destination.difficulty = tag.getFloat(TAG_DIFFICULTY);
        destination.decay = tag.getFloat(TAG_DECAY);

        return destination;
    }
}
