package svenhjol.strange.runestones.module;

import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.helper.ClientHelper;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.meson.helper.SoundHelper;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.outerlands.module.Outerlands;
import svenhjol.strange.runestones.block.RunestoneBlock;
import svenhjol.strange.runestones.capability.*;
import svenhjol.strange.runestones.message.ClientInteractMessage;
import svenhjol.strange.stonecircles.module.StoneCircles;

import java.util.*;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUNESTONES, hasSubscriptions = true)
public class Runestones extends MesonModule
{
    public static RunestoneBlock block;
//    public static List<IDestination> destinations = new ArrayList<>();
    public static List<Destination> dests = new ArrayList<>();
    private static Map<UUID, BlockPos> playerTeleport = new HashMap<>();

    @CapabilityInject(IRunestonesCapability.class)
    public static Capability<IRunestonesCapability> RUNESTONES = null;

    public static ResourceLocation RUNESTONES_CAP_ID = new ResourceLocation(Strange.MOD_ID, "runestone_capability");

    @Override
    public void init()
    {
        block = new RunestoneBlock(this);
    }

    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        // register cap, cap storage and implementation
        CapabilityManager.INSTANCE.register(IRunestonesCapability.class, new RunestonesStorage(), RunestonesCapability::new);

        dests.add(new Destination("spawn_point", false, 1.0F));
        dests.add(new Destination("spawn_point", false, 1.0F));

        if (Strange.loader.hasModule(StoneCircles.class)) {
            dests.add(new Destination(StoneCircles.NAME, "stone_circle", false, 0.8F));
        } else {
            dests.add(new Destination("spawn_point", false, 0.8F));
        }

        dests.add(new Destination("Village", "village", false, 0.8F));
        dests.add(new Destination("Desert_Pyramid", "desert_pyramid", false, 0.75F));
        dests.add(new Destination("Jungle_Pyramid", "jungle_pyramid", false, 0.75F));
        dests.add(new Destination("Ocean_Ruin", "ocean_ruin", false, 0.75F));

        dests.add(new Destination("Village", "outer_village", true, 0.35F));
        dests.add(new Destination("Desert_Pyramid", "outer_desert_pyramid", true, 0.35F));
        dests.add(new Destination("Jungle_Pyramid", "outer_jungle_pyramid", true, 0.35F));
        dests.add(new Destination("Ocean_Ruin", "outer_ocean_ruin", true, 0.35F));

        if (Strange.loader.hasModule(StoneCircles.class)) {
            dests.add(new Destination(StoneCircles.NAME, "outer_stone_circle", true, 0.125F));
        } else {
            dests.add(new Destination("spawn_point", false, 0.125F));
        }



        // first dest is always spawnpoint
//        destinations.add((world, pos, rand) -> world.getSpawnPoint());
//        destinations.add((world, pos, rand) -> world.getSpawnPoint());
//        if (Strange.loader.hasModule(StoneCircles.class)) {
//            destinations.add((world, pos, rand) -> world.findNearestStructure(StoneCircles.NAME, getInnerPos(world, rand), dist, true));
//        } else {
//            destinations.add((world, pos, rand) -> world.getSpawnPoint());
//        }
//
//        destinations.add((world, pos, rand) -> world.findNearestStructure("Village", getInnerPos(world, rand), dist, true));
//        destinations.add((world, pos, rand) -> world.findNearestStructure("Desert_Pyramid", getInnerPos(world, rand), dist, true));
//        destinations.add((world, pos, rand) -> world.findNearestStructure("Jungle_Pyramid", getInnerPos(world, rand), dist, true));
//        destinations.add((world, pos, rand) -> world.findNearestStructure("Ocean_Ruin", getInnerPos(world, rand), dist, true));
//        destinations.add((world, pos, rand) -> world.findNearestStructure("Village", getOuterPos(world, rand), dist, true));
//        destinations.add((world, pos, rand) -> world.findNearestStructure("Desert_Pyramid", getOuterPos(world, rand), dist, true));
//        destinations.add((world, pos, rand) -> world.findNearestStructure("Jungle_Pyramid", getOuterPos(world, rand), dist, true));
//        destinations.add((world, pos, rand) -> world.findNearestStructure("Ocean_Ruin", getOuterPos(world, rand), dist, true));
//
//        if (Strange.loader.hasModule(StoneCircles.class)) {
//            destinations.add((world, pos, rand) -> world.findNearestStructure(StoneCircles.NAME, getOuterPos(world, rand), dist, true));
//        } else {
//            destinations.add((world, pos, rand) -> world.getSpawnPoint());
//        }
    }

    public static IRunestonesCapability getCapability(PlayerEntity player)
    {
        return player.getCapability(RUNESTONES, null).orElse(new DummyCapability());
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
        final int destSize = dests.size();
        for (int i = 0; i < destSize; i++) {
            if (rand.nextFloat() < dests.get(i).weight) {
                return i;
            }
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
                PacketHandler.sendTo(new ClientInteractMessage(ClientInteractMessage.ACTIVATE, pos), (ServerPlayerEntity)player);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (event.phase == Phase.END
            && !event.player.world.isRemote
            && event.player.world.getGameTime() % 10 == 0
        ) {
            // see Entity.java:1433
            PlayerEntity player = event.player;
            World world = player.world;

            int len = 6;
            Vec3d vec3d = player.getEyePosition(1.0F);
            Vec3d vec3d1 = player.getLook(1.0F);
            Vec3d vec3d2 = vec3d.add(vec3d1.x * len, vec3d1.y * len, vec3d1.z * len);
            BlockRayTraceResult result = world.rayTraceBlocks(new RayTraceContext(vec3d, vec3d2, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));

            if (result.getPos() != null) {
                BlockPos runePos = result.getPos();
                BlockState lookedAt = world.getBlockState(runePos);
                if (lookedAt.getBlock() == Runestones.block) {
                    IRunestonesCapability cap = Runestones.getCapability(player);
                    int rune = lookedAt.get(RunestoneBlock.RUNE);
                    if (cap.getDiscoveredTypes().contains(rune)) {
                        String message;
                        String description = I18n.format("runestone.strange." + dests.get(rune).description);
                        BlockPos dest = cap.getDestination(runePos);
                        if (dest != null) {
                            PacketHandler.sendTo(new ClientInteractMessage(ClientInteractMessage.TRAVELLED, runePos), (ServerPlayerEntity)player);
                            message = I18n.format("runestone.strange.rune_travelled", description, dest.getX(), dest.getZ());
                        } else {
                            PacketHandler.sendTo(new ClientInteractMessage(ClientInteractMessage.DISCOVERED, runePos), (ServerPlayerEntity)player);
                            message = I18n.format("runestone.strange.rune_connects", description);
                        }
                        player.sendStatusMessage(new StringTextComponent(message), true);
                    }
                }
            }


            // do teleport if queued
            if (!world.isRemote && !playerTeleport.isEmpty()
                && playerTeleport.containsKey(event.player.getUniqueID())) {

                UUID id = player.getUniqueID();
                BlockPos pos = playerTeleport.get(id);

                teleport(world, player, pos);
                playerTeleport.remove(id);
            }
        }
    }

    @SubscribeEvent
    public void onAttachCaps(AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof PlayerEntity)) return;
        event.addCapability(Runestones.RUNESTONES_CAP_ID, new RunestonesProvider());
    }

    @SubscribeEvent
    public void onPlayerSave(PlayerEvent.SaveToFile event)
    {
        event.getPlayer().getPersistentData().put(
            Runestones.RUNESTONES_CAP_ID.toString(),
            Runestones.getCapability(event.getPlayer()).writeNBT()
        );
    }

    @SubscribeEvent
    public void onPlayerLoad(PlayerEvent.LoadFromFile event)
    {
        Runestones.getCapability(event.getPlayer()).readNBT(
            event.getPlayer().getPersistentData()
                .get(Runestones.RUNESTONES_CAP_ID.toString())
        );
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerEvent.Clone event)
    {
        if (!event.isWasDeath()) return;
        IRunestonesCapability oldCap = Runestones.getCapability(event.getOriginal());
        IRunestonesCapability newCap = Runestones.getCapability(event.getPlayer());
        newCap.readNBT(oldCap.writeNBT());
    }

    private void teleport(World world, PlayerEntity player, BlockPos pos)
    {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof RunestoneBlock)) return;
        int rune = state.get(RunestoneBlock.RUNE);

        Runestones.getCapability(player).discoverType(rune);

        Random rand = world.rand;
        rand.setSeed(pos.toLong());

        BlockPos destPos = dests.get(rune).getDest(world, pos, rand);
        if (destPos == null) destPos = world.getSpawnPoint();

        BlockPos dest = addRandomOffset(destPos, rand, 8);
        int dim = WorldHelper.getDimensionId(world);

        PlayerHelper.teleportSurface(player, dest, dim, p -> {
            Runestones.getCapability(player).recordDestination(pos, dest);
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static void effectActivate(BlockPos pos)
    {
        ClientWorld world = ClientHelper.getClientWorld();

        double spread = 0.75D;
        for (int i = 0; i < 10; i++) {
            double px = pos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
            double py = pos.getY() + 0.5D + (Math.random() - 0.5D) * spread;
            double pz = pos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
            world.addParticle(ParticleTypes.PORTAL, px, py, pz, 0.3D, 0.3D, 0.3D);
        }
        SoundHelper.playSoundAtPos(pos, StrangeSounds.RUNESTONE_TRAVEL, SoundCategory.PLAYERS, 0.6F, 1.05F);
    }

    @OnlyIn(Dist.CLIENT)
    public static void effectTravelled(BlockPos pos)
    {
        ClientWorld world = ClientHelper.getClientWorld();

        double spread = 1.75D;
        for (int i = 0; i < 10; i++) {
            double px = pos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
            double py = pos.getY() + 0.25D + (Math.random() - 0.25D) * spread;
            double pz = pos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
            world.addParticle(ParticleTypes.ENCHANT, px, py, pz, 0.1D, 0.1D, 0.1D);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void effectDiscovered(BlockPos pos)
    {
        ClientWorld world = ClientHelper.getClientWorld();

        double spread = 1.75D;
        if (new Random().nextFloat() < 0.35F) {
            for (int i = 0; i < 2; i++) {
                double px = pos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
                double py = pos.getY() + 0.25D + (Math.random() - 0.25D) * spread;
                double pz = pos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
                world.addParticle(ParticleTypes.ENCHANT, px, py, pz, 0.02D, 0.02D, 0.02D);
            }
        }
    }

    public static class Destination
    {
        private final static String SPAWN = "spawn";
        private final static int dist = 100;

        public String structure;
        public String description;
        public boolean outerlands;
        public float weight;

        public Destination(String structure, String description, boolean outerlands, float weight)
        {
            this.structure = structure;
            this.description = description;
            this.outerlands = outerlands;
            this.weight = weight;
        }

        public Destination(String description, boolean outerlands, float weight)
        {
            this(SPAWN, description, outerlands, weight);
        }

        public BlockPos getDest(World world, BlockPos runePos, Random rand)
        {
            if (this.structure.equals(SPAWN)) {
                return world.getSpawnPoint();
            }

            BlockPos target = outerlands ? getOuterPos(world, rand) : getInnerPos(world, rand);
            BlockPos dest = world.findNearestStructure(structure, target, dist, true);

            return dest == null ? world.getSpawnPoint() : dest;
        }
    }
}
