package svenhjol.strange.travelrunes.module;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.travelrunes.block.BaseRunestoneBlock;
import svenhjol.strange.travelrunes.block.EndRunestoneBlock;
import svenhjol.strange.travelrunes.block.NetherRunestoneBlock;
import svenhjol.strange.travelrunes.block.StoneRunestoneBlock;
import svenhjol.strange.travelrunes.feature.StoneCircleFeature;

import java.util.*;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TRAVEL_RUNES, hasSubscriptions = true)
public class Runestones extends MesonModule
{
    public static int inner = 100000;
    public static int outer = 20000000;

    public static BaseRunestoneBlock stone;
    public static BaseRunestoneBlock nether;
    public static BaseRunestoneBlock end;

    private static Map<UUID, BlockPos> playerTeleport = new HashMap<>();
    private List<Destination> destinations = new ArrayList<>();

    @Override
    public void init()
    {
        stone = new StoneRunestoneBlock(this);
        nether = new NetherRunestoneBlock(this);
        end = new EndRunestoneBlock(this);
    }

    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        StoneCircleFeature feature = new StoneCircleFeature(NoFeatureConfig::deserialize);
        GenerationStage.Decoration stage = GenerationStage.Decoration.TOP_LAYER_MODIFICATION;
        ConfiguredFeature<?> decoration = Biome.createDecoratedFeature(feature, IFeatureConfig.NO_FEATURE_CONFIG, Placement.COUNT_HEIGHTMAP_DOUBLE, new FrequencyConfig(1));

        Registry.BIOME.stream().forEach(biome -> {
            biome.addFeature(stage, decoration);
        });

        destinations.add((world, pos, rand) -> world.getSpawnPoint());
        destinations.add((world, pos, rand) -> world.getSpawnPoint());
        destinations.add((world, pos, rand) -> world.getSpawnPoint());
        destinations.add((world, pos, rand) -> world.getSpawnPoint());
        destinations.add((world, pos, rand) -> world.findNearestStructure("Village", getInnerPos(rand), 100, true));
        destinations.add((world, pos, rand) -> world.findNearestStructure("Desert_Pyramid", getInnerPos(rand), 100, true));
        destinations.add((world, pos, rand) -> world.findNearestStructure("Jungle_Temple", getInnerPos(rand), 100, true));
        destinations.add((world, pos, rand) -> world.findNearestStructure("Ocean_Ruin", getInnerPos(rand), 100, true));
        destinations.add((world, pos, rand) -> world.findNearestStructure("Village", getOuterPos(rand), 100, true));
        destinations.add((world, pos, rand) -> world.findNearestStructure("Desert_Pyramid", getOuterPos(rand), 100, true));
        destinations.add((world, pos, rand) -> world.findNearestStructure("Jungle_Temple", getOuterPos(rand), 100, true));
        destinations.add((world, pos, rand) -> world.findNearestStructure("Ocean_Ruin", getOuterPos(rand), 100, true));
    }

    private BlockPos getInnerPos(Random rand)
    {
        int x = rand.nextInt(inner * 2) - inner;
        int z = rand.nextInt(inner * 2) - inner;
        return new BlockPos(x, 0, z);
    }

    private BlockPos getOuterPos(Random rand)
    {
        int x = rand.nextInt(outer * 2) - outer;
        int z = rand.nextInt(outer * 2) - outer;

        x += rand.nextFloat() < 0.5 ? inner : -inner;
        z += rand.nextFloat() < 0.5 ? inner : -inner;

        return new BlockPos(x, 0, z);
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

            if (world.getBlockState(pos).getBlock() instanceof BaseRunestoneBlock) {
                // prepare teleport
                playerTeleport.put(player.getUniqueID(), pos);
            }
        }
    }

    @SubscribeEvent
    public void onTeleport(EnderTeleportEvent event)
    {
        if (!event.getEntity().world.isRemote
            && event.getEntityLiving() instanceof PlayerEntity
            && playerTeleport.containsKey(event.getEntityLiving().getUniqueID())
        ) {
            World world = event.getEntity().world;
            PlayerEntity player = (PlayerEntity) event.getEntity();
            UUID id = player.getUniqueID();
            BlockPos pos = playerTeleport.get(id);

            teleport(world, player, pos);

            playerTeleport.remove(id);
            event.setCanceled(true);
        }
    }

    private void teleport(World world, PlayerEntity player, BlockPos pos)
    {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BaseRunestoneBlock)) return;
        int rune = state.get(BaseRunestoneBlock.RUNE);

        Random rand = world.rand;
        rand.setSeed(pos.toLong());
        BlockPos dest = addRandomOffset(destinations.get(rune).get(world, pos, rand), rand, 20); // TODO null

        if (world.dimension.getType() != DimensionType.OVERWORLD) {
            player.changeDimension(DimensionType.OVERWORLD);
        }
        ((ServerPlayerEntity)player).teleport((ServerWorld)world, dest.getX(), dest.getY(), dest.getZ(), player.rotationYaw, player.rotationPitch);
        BlockPos updateDest = world.getHeight(Heightmap.Type.MOTION_BLOCKING, dest);
        player.setPositionAndUpdate(updateDest.getX(), updateDest.getY() + 1, updateDest.getZ()); // TODO check landing block

    }

    public interface Destination
    {
        BlockPos get(World world, BlockPos target, Random rand);
    }
}
