package svenhjol.strange.module.astrolabes;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;
import net.minecraft.world.phys.AABB;
import svenhjol.charm.helper.DimensionHelper;

import java.util.List;
import java.util.stream.Stream;

public class AstrolabeBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
    public static final String DIMENSION_NBT = "Dimension";
    public static final String POSITION_NBT = "Position";

    public ResourceKey<Level> dimension = Level.OVERWORLD;
    public BlockPos position = BlockPos.ZERO;

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        this.dimension = DimensionHelper.decodeDimension(nbt.get(DIMENSION_NBT)).orElse(Level.OVERWORLD);
        this.position = BlockPos.of(nbt.getLong(POSITION_NBT));
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        super.save(nbt);

        DimensionHelper.encodeDimension(this.dimension, el -> {
            nbt.put(DIMENSION_NBT, el);
        });

        nbt.putLong(POSITION_NBT, this.position.asLong());
        return nbt;
    }

    public AstrolabeBlockEntity(BlockPos position, BlockState state) {
        super(Astrolabes.BLOCK_ENTITY, position, state);
    }

    @Override
    public void fromClientTag(CompoundTag nbt) {
        load(nbt);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag nbt) {
        return save(nbt);
    }

    public static <T extends AstrolabeBlockEntity> void tick(Level world, BlockPos pos, BlockState state, T astrolabe) {
        if (world == null || world.getGameTime() % 100 != 0)
            return;

        if (!world.isClientSide && astrolabe.position != BlockPos.ZERO) {
            BlockPos position = Astrolabes.getDimensionPosition(world, astrolabe.position, astrolabe.dimension);
            FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
            data.writeBoolean(false); // don't play sound
            data.writeLongArray(Stream.of(position).map(BlockPos::asLong).mapToLong(Long::longValue).toArray());

            AABB bb = (new AABB(pos)).inflate(16);
            List<Player> list = world.getEntitiesOfClass(Player.class, bb);
            list.forEach(player -> {
                ((ServerLevel)world).sendVibrationParticle(new VibrationPath(pos, new BlockPositionSource(position), 10));
                ServerPlayNetworking.send((ServerPlayer)player, Astrolabes.MSG_CLIENT_SHOW_AXIS_PARTICLES, data);
            });
        }
    }
}
