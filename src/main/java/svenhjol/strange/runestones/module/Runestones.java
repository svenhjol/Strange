package svenhjol.strange.runestones.module;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.helper.ClientHelper;
import svenhjol.meson.helper.SoundHelper;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.base.message.RunestoneActivated;
import svenhjol.strange.outerlands.module.Outerlands;
import svenhjol.strange.runestones.block.RunestoneBlock;
import svenhjol.strange.stonecircles.module.StoneCircles;

import java.util.*;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUNESTONES, hasSubscriptions = true)
public class Runestones extends MesonModule
{
    public static RunestoneBlock block;
    public static List<Destination> destinations = new ArrayList<>();
    private static Map<UUID, BlockPos> playerTeleport = new HashMap<>();

    @Override
    public void init()
    {
        block = new RunestoneBlock(this);
    }

    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        int dist = 100;

        // first dest is always spawnpoint
        destinations.add((world, pos, rand) -> world.getSpawnPoint());
        destinations.add((world, pos, rand) -> world.getSpawnPoint());
        if (Strange.loader.hasModule(StoneCircles.class)) {
            destinations.add((world, pos, rand) -> world.findNearestStructure(StoneCircles.NAME, getInnerPos(world, rand), dist, true));
        } else {
            destinations.add((world, pos, rand) -> world.getSpawnPoint());
        }

        destinations.add((world, pos, rand) -> world.findNearestStructure("Village", getInnerPos(world, rand), dist, true));
        destinations.add((world, pos, rand) -> world.findNearestStructure("Desert_Pyramid", getInnerPos(world, rand), dist, true));
        destinations.add((world, pos, rand) -> world.findNearestStructure("Jungle_Pyramid", getInnerPos(world, rand), dist, true));
        destinations.add((world, pos, rand) -> world.findNearestStructure("Ocean_Ruin", getInnerPos(world, rand), dist, true));
        destinations.add((world, pos, rand) -> world.findNearestStructure("Village", getOuterPos(world, rand), dist, true));
        destinations.add((world, pos, rand) -> world.findNearestStructure("Desert_Pyramid", getOuterPos(world, rand), dist, true));
        destinations.add((world, pos, rand) -> world.findNearestStructure("Jungle_Pyramid", getOuterPos(world, rand), dist, true));
        destinations.add((world, pos, rand) -> world.findNearestStructure("Ocean_Ruin", getOuterPos(world, rand), dist, true));

        if (Strange.loader.hasModule(StoneCircles.class)) {
            destinations.add((world, pos, rand) -> world.findNearestStructure(StoneCircles.NAME, getOuterPos(world, rand), dist, true));
        } else {
            destinations.add((world, pos, rand) -> world.getSpawnPoint());
        }
    }

    public static int getRunestoneType(IWorld world)
    {
        DimensionType type = world.getDimension().getType();

        if (type == DimensionType.THE_END) {
            return 2;
        } else if (type == DimensionType.THE_NETHER) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int getRunestoneValue(IWorld world, BlockPos pos, Random rand)
    {
        final int destSize = destinations.size();
        for (int i = 0; i < destSize; i++) {
            if (rand.nextFloat() < 0.2F) return i;
        }
        return 0;
    }

    public static BlockState getRunestoneBlock(IWorld world, BlockPos pos, Random rand)
    {
        return block.getDefaultState()
            .with(RunestoneBlock.TYPE, getRunestoneType(world))
            .with(RunestoneBlock.RUNE, getRunestoneValue(world, pos, rand));
    }

    public static BlockState getRunestoneBlock(IWorld world, int runeValue)
    {
        return block.getDefaultState()
            .with(RunestoneBlock.TYPE, getRunestoneType(world))
            .with(RunestoneBlock.RUNE, runeValue);
    }

    public static BlockPos getInnerPos(World world, Random rand)
    {
        if (Strange.loader.hasModule(Outerlands.class)) {
            return Outerlands.getInnerPos(world, rand);
        } else {
            int max = 10000;
            int x = rand.nextInt(max * 2) - max;
            int z = rand.nextInt(max * 2) - max;
            return new BlockPos(x, 0, z);
        }
    }

    public static BlockPos getOuterPos(World world, Random rand)
    {
        if (Strange.loader.hasModule(Outerlands.class)) {
            return Outerlands.getOuterPos(world, rand);
        } else {
            int max = 100000;
            int x = rand.nextInt(max * 2) - max;
            int z = rand.nextInt(max * 2) - max;
            return new BlockPos(x, 0, z);
        }
    }

    private BlockPos addRandomOffset(BlockPos pos, Random rand, int max)
    {
        int n = rand.nextInt(max);
        int e = rand.nextInt(max);
        int s = rand.nextInt(max);
        int w = rand.nextInt(max);
        pos = pos.north(rand.nextFloat() < 0.5F ? n : -n);
        pos = pos.east(rand.nextFloat() < 0.5F ? e : -e);
        pos = pos.south(rand.nextFloat() < 0.5F ? s : -s);
        pos = pos.west(rand.nextFloat() < 0.5F ? w : -w);
        return pos;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPearlImpact(ProjectileImpactEvent event)
    {
        if (!event.getEntity().world.isRemote
            && event.getEntity() instanceof EnderPearlEntity
            && event.getRayTraceResult().getType() == RayTraceResult.Type.BLOCK
            && ((ProjectileItemEntity) event.getEntity()).getThrower() instanceof PlayerEntity
        ) {
            World world = event.getEntity().world;
            BlockPos pos = ((BlockRayTraceResult) event.getRayTraceResult()).getPos();
            PlayerEntity player = (PlayerEntity) ((ProjectileItemEntity) event.getEntity()).getThrower();

            if (player == null) return;

            if (world.getBlockState(pos).getBlock() instanceof RunestoneBlock) {
                event.setCanceled(true);
                event.getEntity().remove();

                playerTeleport.put(player.getUniqueID(), pos); // prepare teleport, tick handles it
                PacketHandler.sendTo(new RunestoneActivated(pos), (ServerPlayerEntity)player);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (event.phase == Phase.END
            && !event.player.world.isRemote
            && event.player.world.getGameTime() % 5 == 0
            && !playerTeleport.isEmpty()
            && playerTeleport.containsKey(event.player.getUniqueID())
        ) {
            World world = event.player.world;
            PlayerEntity player = event.player;
            UUID id = player.getUniqueID();
            BlockPos pos = playerTeleport.get(id);

            teleport(world, player, pos);

            playerTeleport.remove(id);
        }
    }

    private void teleport(World world, PlayerEntity player, BlockPos pos)
    {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof RunestoneBlock)) return;
        int rune = state.get(RunestoneBlock.RUNE);

        Random rand = world.rand;
        rand.setSeed(pos.toLong());

        BlockPos destPos = destinations.get(rune).get(world, pos, rand);

        // TODO really lame
        if (destPos == null) destPos = world.getSpawnPoint();

        BlockPos dest = addRandomOffset(destPos, rand, 8);
//        PlayerHelper.teleportPlayer(player, dest, WorldHelper.getDimensionId(world));

        int dim = WorldHelper.getDimensionId(world);

        if (player.dimension.getId() != dim && !world.isRemote && !player.isPassenger() && !player.isBeingRidden() && player.isNonBoss()) {
            DimensionType dimension = DimensionType.getById(dim);
            if (dimension != null) {
                player.changeDimension(dimension);
            }
        }

        ChunkPos chunkpos = new ChunkPos(new BlockPos(dest.getX(), dest.getY(), dest.getZ()));
        ((ServerWorld)world).getChunkProvider().func_217228_a(TicketType.POST_TELEPORT, chunkpos, 1, player.getEntityId());

        Minecraft.getInstance().deferTask(() -> {

            ((ServerPlayerEntity)player).teleport((ServerWorld)world, (double)dest.getX(), (double)dest.getY(), (double)dest.getZ(), player.rotationYaw, player.rotationPitch);
            for (int y = world.getHeight(); y > 0; y--) {
                BlockPos pp = new BlockPos(dest.getX(), y, dest.getZ());
                if (world.getBlockState(pp).isAir()
                    && !world.getBlockState(pp.down()).isAir()
                ) {
                    player.setPositionAndUpdate((double)pp.getX(), y, (double)pp.getZ());
                    break;
                }
            }
        });

//        BlockPos updateDest = world.getHeight(Heightmap.Type.MOTION_BLOCKING, dest);
//        player.setPositionAndUpdate((double)updateDest.getX(), (double)(updateDest.getY() + 1), (double)updateDest.getZ());
    }

    @OnlyIn(Dist.CLIENT)
    public static void effectActivate(BlockPos pos)
    {
        ClientWorld world = ClientHelper.getClientWorld();

        double spread = 0.75D;
        for (int i = 0; i < 10; i++) {
            double px = pos.getX() + 0.25D + (Math.random() - 0.5D) * spread;
            double py = pos.getY() + 1.5D + (Math.random() - 0.5D) * spread;
            double pz = pos.getZ() + 0.25D + (Math.random() - 0.5D) * spread;
            world.addParticle(ParticleTypes.PORTAL, px, py, pz, 0.3D, 0.3D, 0.3D);
        }
        SoundHelper.playSoundAtPos(pos, StrangeSounds.RUNESTONE_TRAVEL, SoundCategory.PLAYERS, 0.6F, 1.05F);
    }

    public interface Destination
    {
        BlockPos get(World world, BlockPos target, Random rand);
    }
}
