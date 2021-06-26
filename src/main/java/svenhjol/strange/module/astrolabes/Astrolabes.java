package svenhjol.strange.module.astrolabes;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;
import svenhjol.charm.annotation.Module;
import svenhjol.charm.event.PlayerTickCallback;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.module.CharmModule;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Module(mod = Strange.MOD_ID, client = AstrolabesClient.class, description = "Place an astrolabe and link another to it to help align builds.")
public class Astrolabes extends CharmModule {
    public static final ResourceLocation ID = new ResourceLocation(Strange.MOD_ID, "astrolabe");
    public static final ResourceLocation MSG_CLIENT_SHOW_AXIS_PARTICLES = new ResourceLocation(Strange.MOD_ID, "client_show_axis_particles");
    public static final ResourceLocation TRIGGER_LINKED_ASTROLABE = new ResourceLocation(Strange.MOD_ID, "linked_astrolabe");
    public static final ResourceLocation TRIGGER_LOCATED_ASTROLABE_LOCATION = new ResourceLocation(Strange.MOD_ID, "located_astrolabe_location");

    public static AstrolabeBlock ASTROLABE;
    public static BlockEntityType<AstrolabeBlockEntity> BLOCK_ENTITY;
    public static PoiType POIT;

    @Override
    public void register() {
        ASTROLABE = new AstrolabeBlock(this);
        BLOCK_ENTITY = RegistryHelper.blockEntity(ID, AstrolabeBlockEntity::new, ASTROLABE);
        POIT = WorldHelper.addPointOfInterestType(ID, ASTROLABE, 0);
    }

    @Override
    public void init() {
        PlayerTickCallback.EVENT.register(this::handlePlayerTick);
    }

    private void handlePlayerTick(Player player) {
        if (player.level.isClientSide || player.level.getGameTime() % 100 != 0)
            return;

        ServerLevel serverWorld = (ServerLevel)player.level;
        List<BlockPos> positions = new ArrayList<>();

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(hand);
            if (!(held.getItem() instanceof AstrolabeBlockItem))
                continue;

            Optional<ResourceKey<Level>> dimension = AstrolabeBlockItem.getDimension(held);
            Optional<BlockPos> position = AstrolabeBlockItem.getPosition(held);

            if (dimension.isEmpty() || position.isEmpty())
                continue;

            ResourceKey<Level> dim = dimension.get();
            BlockPos pos = position.get();
            ServerLevel dimWorld = serverWorld.getServer().getLevel(dim);

            if (dimWorld == null || !dimWorld.getPoiManager().existsAtPosition(Astrolabes.POIT, pos)) {
                held.getOrCreateTag().remove(AstrolabeBlockItem.POSITION_NBT);
                held.getOrCreateTag().remove(AstrolabeBlockItem.DIMENSION_NBT);
                continue;
            }

            BlockPos.MutableBlockPos dimensionPos = getDimensionPosition(player.level, pos, dim);
            positions.add(dimensionPos);

            // do the dimensional anchor advancement
            if (player.blockPosition().equals(dimensionPos) && !player.level.dimension().equals(dim))
                triggerLocatedAstrolabeLocation((ServerPlayer) player);
        }

        if (!positions.isEmpty()) {
            FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
            data.writeBoolean(true); // play sound
            data.writeLongArray(positions.stream().distinct().map(BlockPos::asLong).mapToLong(Long::longValue).toArray());
            ServerPlayNetworking.send((ServerPlayer) player, MSG_CLIENT_SHOW_AXIS_PARTICLES, data);

            // TODO: move to shared method
            serverWorld.sendVibrationParticle(new VibrationPath(player.blockPosition(), new BlockPositionSource(positions.get(0)), 20));
        }
    }

    public static BlockPos.MutableBlockPos getDimensionPosition(Level currentWorld, BlockPos astrolabePos, ResourceKey<Level> astrolabeDimension) {
        BlockPos.MutableBlockPos position = astrolabePos.mutable();

        if (astrolabeDimension != null) {
            // if the astrolabe was set in the nether and the user is not in the nether, multiply X and Z
            if (astrolabeDimension.equals(Level.NETHER) && currentWorld.dimension() != Level.NETHER) {
                int x = position.getX();
                int y = position.getY();
                int z = position.getZ();

                position.set(x * 8, y, z * 8);
            }

            // if the astrolabe was set outside the nether and the user is in the nether, divide X and Z
            if (!astrolabeDimension.equals(Level.NETHER) && currentWorld.dimension() == Level.NETHER) {
                int x = position.getX();
                int y = position.getY();
                int z = position.getZ();

                position.set(x / 8, y, z / 8);
            }
        }

        return position;
    }

    public static void triggerLinkedAstrolabe(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_LINKED_ASTROLABE);
    }

    public static void triggerLocatedAstrolabeLocation(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_LOCATED_ASTROLABE_LOCATION);
    }
}
