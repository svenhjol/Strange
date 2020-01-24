package svenhjol.strange.runestones.module;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
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
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.StrangeLoader;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.outerlands.module.Outerlands;
import svenhjol.strange.ruins.module.UndergroundRuins;
import svenhjol.strange.runestones.block.RunestoneBlock;
import svenhjol.strange.runestones.capability.*;

import java.util.*;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUNESTONES, hasSubscriptions = true)
public class Runestones extends MesonModule
{
    public static ResourceLocation RUNESTONES_CAP_ID = new ResourceLocation(Strange.MOD_ID, "runestone_capability");

    public static List<Destination> innerDests = new ArrayList<>();
    public static List<Destination> outerDests = new ArrayList<>();
    public static List<Destination> allDests = new ArrayList<>();
    public static List<Destination> ordered = new ArrayList<>();
    public static Map<UUID, BlockPos> playerTeleport = new HashMap<>();
    public static RunestoneBlock block;
    public long seed = 0;

    @Config(name = "Runestone for Quark Big Dungeons", description = "If true, one of the runestones will link to a Big Dungeon from Quark.\n" +
        "This module must be enabled in Quark for the feature to work.\n" +
        "If false, or Quark is not available, this runestone destination will be a stone circle instead.")
    public static boolean useQuarkBigDungeon = true;

    @CapabilityInject(IRunestonesCapability.class)
    public static Capability<IRunestonesCapability> RUNESTONES = null;

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
    }

    @Override
    public void serverAboutToStart(FMLServerAboutToStartEvent event)
    {
        // add all destinations here; serverStarted shuffles them according to world seed
        ordered = new ArrayList<>();

        ordered.add(new Destination("spawn_point", false, 1.0F));
        ordered.add(new Destination("spawn_point", true, 1.0F));
        ordered.add(new Destination(StoneCircles.RESNAME, "stone_circle", false, 0.9F));
        ordered.add(new Destination(StoneCircles.RESNAME, "outer_stone_circle", true, 0.9F));
        ordered.add(new Destination("Village", "village", false, 0.5F));
        ordered.add(new Destination("Village", "outer_village", true, 0.7F));
        ordered.add(new Destination("Pillager_Outpost", "pillager_outpost", false, 0.75F));
        ordered.add(new Destination("Pillager_Outpost", "outer_pillager_outpost", true, 0.6F));
        ordered.add(new Destination("Desert_Pyramid", "desert_pyramid", false, 0.2F));
        ordered.add(new Destination("Jungle_Pyramid", "jungle_pyramid", false, 0.2F));
        ordered.add(new Destination("Ocean_Ruin", "ocean_ruin", false, 0.65F));
        ordered.add(new Destination("Mineshaft", "mineshaft", false, 0.5F));
        ordered.add(new Destination("Swamp_Hut", "swamp_hut", false, 0.3F));
        ordered.add(new Destination("Igloo", "igloo", false, 0.3F));

        if (useQuarkBigDungeon
            && StrangeLoader.quarkBigDungeons != null
            && StrangeLoader.quarkBigDungeons.hasModule()
        ) {
            ordered.add(new Destination(StrangeLoader.quarkBigDungeons.getResName(), "big_dungeon", false, 0.3F));
            Meson.debug("Added Quark's Big Dungeons as a runestone destination");
        } else {
            ordered.add(new Destination(StoneCircles.RESNAME, "stone_circle", false, 0.3F));
        }

        if (Strange.hasModule(UndergroundRuins.class)) {
            ordered.add(new Destination(UndergroundRuins.RESNAME, "underground_ruin", false, 0.15F));
        } else {
            ordered.add(new Destination(StoneCircles.RESNAME, "stone_circle", false, 0.15F));
        }
    }

    @Override
    public void serverStarted(FMLServerStartedEvent event)
    {
        super.serverStarted(event);
        long seed = event.getServer().getWorld(DimensionType.OVERWORLD).getSeed();

        allDests = new ArrayList<>();
        outerDests = new ArrayList<>();
        innerDests = new ArrayList<>();

        if (seed != this.seed) {
            Random rand = new Random();
            rand.setSeed(seed);

            for (Destination dest : ordered) {
                if (dest.outerlands) {
                    outerDests.add(dest);
                } else {
                    innerDests.add(dest);
                }
            }

            Collections.shuffle(innerDests, rand);
            Collections.shuffle(outerDests, rand);
            allDests.addAll(innerDests);
            allDests.addAll(outerDests);

            this.seed = seed;
        }
    }

    public static IRunestonesCapability getCapability(PlayerEntity player)
    {
        return player.getCapability(RUNESTONES, null).orElse(new DummyCapability());
    }

    public static int getRunestoneType(IWorld world)
    {
        return getRunestoneType(world.getDimension().getType());
    }

    public static int getRunestoneType(DimensionType type)
    {
        if (type == DimensionType.THE_END) {
            return 2;
        } else if (type == DimensionType.THE_NETHER) {
            return 1;
        } else {
            return 0;
        }
    }

    public static BlockState getRunestoneBlock(IWorld world, int runeValue)
    {
        return block.getDefaultState()
            .with(RunestoneBlock.TYPE, getRunestoneType(world))
            .with(RunestoneBlock.RUNE, runeValue);
    }

    public static BlockState getRunestoneBlock(DimensionType dim, int runeValue)
    {
        return block.getDefaultState()
            .with(RunestoneBlock.TYPE, getRunestoneType(dim))
            .with(RunestoneBlock.RUNE, runeValue);
    }

    public static BlockState getRandomBlock(DimensionType dim)
    {
        return getRunestoneBlock(dim, new Random().nextInt(ordered.size()));
    }

    public static BlockState getRandomBlock(DimensionType dim, BlockPos pos)
    {
        List<Destination> pool = Outerlands.isOuterPos(pos) ? allDests : innerDests;
        return getRunestoneBlock(dim, new Random(pos.toLong()).nextInt(pool.size()));
    }

    public static BlockPos getInnerPos(World world, Random rand)
    {
        if (Strange.hasModule(Outerlands.class)) {
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
        if (Strange.hasModule(Outerlands.class)) {
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
                effectActivate(world, pos);
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

            BlockPos runePos = result.getPos();
            BlockState lookedAt = world.getBlockState(runePos);
            if (lookedAt.getBlock() == Runestones.block) {
                IRunestonesCapability cap = Runestones.getCapability(player);
                int rune = lookedAt.get(RunestoneBlock.RUNE);
                if (cap.getDiscoveredTypes().contains(rune)) {
                    TranslationTextComponent message;
                    TranslationTextComponent description = new TranslationTextComponent("runestone.strange." + allDests.get(rune).description);
                    BlockPos dest = cap.getDestination(runePos);
                    if (dest != null) {
                        effectTravelled(world, runePos);
                        message = new TranslationTextComponent("runestone.strange.rune_travelled", description, dest.getX(), dest.getZ());
                    } else {
                        effectDiscovered(world, runePos);
                        message = new TranslationTextComponent("runestone.strange.rune_connects", description);
                    }
                    player.sendStatusMessage(message, true);
                }
            }

            // do teleport if queued
            if (!world.isRemote && !playerTeleport.isEmpty()
                && playerTeleport.containsKey(event.player.getUniqueID())) {

                UUID id = player.getUniqueID();
                BlockPos pos = playerTeleport.get(id);

                prepareTeleport(world, player, pos);
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
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        Runestones.getCapability(event.getPlayer()).readNBT(
            event.getPlayer().getPersistentData()
                .get(Runestones.RUNESTONES_CAP_ID.toString())
        );
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event)
    {
        event.getPlayer().getPersistentData().put(
            Runestones.RUNESTONES_CAP_ID.toString(),
            Runestones.getCapability(event.getPlayer()).writeNBT()
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

    private void prepareTeleport(World world, PlayerEntity player, BlockPos pos)
    {
        ServerWorld serverWorld = (ServerWorld)world;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;

        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof RunestoneBlock)) return;
        int rune = state.get(RunestoneBlock.RUNE);

        CriteriaTriggers.ENTER_BLOCK.trigger((ServerPlayerEntity)player, state);

        Runestones.getCapability(player).discoverType(rune);

        Random rand = world.rand;
        rand.setSeed(pos.toLong());

        int dim = player.dimension.getId();
        if (dim != 0) {
            PlayerHelper.changeDimension(player, 0);
            serverWorld = serverPlayer.server.getWorld(DimensionType.OVERWORLD);
        }

        teleport(serverWorld, pos, player, rand, rune);
    }

    private void teleport(World world, BlockPos pos, PlayerEntity player, Random rand, int rune)
    {
        Meson.debug("Rune is " + rune + ", dest is " + allDests.get(rune));

        BlockPos destPos = allDests.get(rune).getDest(world, pos, rand);
        if (destPos == null) destPos = world.getSpawnPoint();
        BlockPos dest = addRandomOffset(destPos, rand, 8);

        PlayerHelper.teleportSurface(player, dest, 0, p -> {
            Runestones.getCapability(player).recordDestination(pos, dest);
        });
    }

    public static void effectActivate(World world, BlockPos pos)
    {
        double spread = 0.75D;
        double px = pos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
        double py = pos.getY() + 0.5D + (Math.random() - 0.5D) * spread;
        double pz = pos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
        ((ServerWorld)world).spawnParticle(ParticleTypes.PORTAL, px, py, pz, 10,0.3D, 0.3D, 0.3D, 0.3D);
        world.playSound(null, pos, StrangeSounds.RUNESTONE_TRAVEL, SoundCategory.PLAYERS, 0.6F, 1.05F);
    }

    public static void effectTravelled(World world, BlockPos pos)
    {
        double spread = 1.5D;
        double px = pos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
        double py = pos.getY() + 0.5D + (Math.random() - 0.5D) * spread;
        double pz = pos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
        ((ServerWorld)world).spawnParticle(ParticleTypes.ENCHANT, px, py, pz, 10,0.2D, 0.6D, 0.2D, 0.45D);
    }

    public static void effectDiscovered(World world, BlockPos pos)
    {
        double spread = 1.75D;
        if (world.rand.nextFloat() < 0.6F) {
            double px = pos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
            double py = pos.getY() + 0.5D + (Math.random() - 0.5D) * spread;
            double pz = pos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
            ((ServerWorld)world).spawnParticle(ParticleTypes.ENCHANT, px, py, pz, 3, 0.15D, 0.3D, 0.15D, 0.2D);
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
            Meson.debug("structure = " + structure);

            if (this.structure.equals(SPAWN)) {
                return world.getSpawnPoint();
            }

            BlockPos target = outerlands ? getOuterPos(world, rand) : getInnerPos(world, rand);
            BlockPos dest = world.findNearestStructure(structure, target, dist, true);

            return dest == null ? world.getSpawnPoint() : dest;
        }
    }
}
