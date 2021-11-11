package svenhjol.strange.module.knowledge.types;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.knowledge.KnowledgeHelper;

import java.util.Optional;

public class Discovery {
    public static final String TAG_RUNES = "Runes";
    public static final String TAG_ID = "Id";
    public static final String TAG_POSITION = "Pos";
    public static final String TAG_DIMENSION = "Dim";
    public static final String TAG_PLAYER = "Player";
    public static final String TAG_DIFFICULTY = "Difficulty";
    public static final String TAG_DECAY = "Decay";

    private final String runes;
    private final ResourceLocation id;
    private BlockPos pos;
    private ResourceLocation dimension;
    private String player;
    private float difficulty;
    private float decay;

    private long cachedSeed;

    public Discovery(String runes, ResourceLocation id) {
        this.runes = runes;
        this.id = id;
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

        if (id != null) {
            tag.putString(TAG_ID, id.toString());
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

    public ResourceLocation getId() {
        return id;
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

    public static Discovery fromTag(CompoundTag tag) {
        String runes = tag.getString(TAG_RUNES);
        ResourceLocation location = new ResourceLocation(tag.getString(TAG_ID));
        Discovery discovery = new Discovery(runes, location);

        String dimensionFromTag = tag.getString(TAG_DIMENSION);
        discovery.dimension = dimensionFromTag.isEmpty() ? null : new ResourceLocation(dimensionFromTag);

        BlockPos pos = BlockPos.of(tag.getLong(TAG_POSITION));
        discovery.pos = pos.equals(BlockPos.ZERO) ? null : pos;

        String playerFromTag = tag.getString(TAG_PLAYER);
        discovery.player = playerFromTag.isEmpty() ? null : tag.getString(TAG_PLAYER);

        discovery.difficulty = tag.getFloat(TAG_DIFFICULTY);
        discovery.decay = tag.getFloat(TAG_DECAY);

        return discovery;
    }
}
