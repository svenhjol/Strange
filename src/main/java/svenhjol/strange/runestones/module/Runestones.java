package svenhjol.strange.runestones.module;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.Feature;
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
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.meson.helper.StringHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.base.helper.LocationHelper;
import svenhjol.strange.base.helper.RunestoneHelper;
import svenhjol.strange.outerlands.module.Outerlands;
import svenhjol.strange.runestones.Destination;
import svenhjol.strange.runestones.block.BaseRunestoneBlock;
import svenhjol.strange.runestones.block.RunestoneBlock;
import svenhjol.strange.runestones.capability.*;
import svenhjol.strange.runestones.tileentity.RunestoneTileEntity;

import javax.annotation.Nullable;
import java.util.*;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUNESTONES, hasSubscriptions = true,
    description = "Runestones allow fast travel to points of interest in your world by using an Ender Pearl.")
public class Runestones extends MesonModule {
    public static final ResourceLocation RUNESTONES_CAP_ID = new ResourceLocation(Strange.MOD_ID, "runestone_capability");
    public static final List<RunestoneBlock> runestones = new ArrayList<>();
    public static List<Destination> destinations = new ArrayList<>();
    public static List<Destination> ordered = new ArrayList<>();
    public static final Map<UUID, BlockPos> playerTeleportRunestone = new HashMap<>();
    private static final int INTERVAL = 10;
    private static final int numberOfRunes = 26; // TODO maybe this should be configurable?

    @Config(name = "Maximum runestone travel distance", description = "Maximum number of blocks that you will be transported from a stone circle runestone.")
    public static int maxDist = 4000;

    @Config(name = "Travel protection duration", description = "Number of seconds of regeneration and slow-fall when travelling through a stone circle runestone.")
    public static int protectionDuration = 10;

    @Config(name = "Available destinations", description = "Destinations that runestones may teleport you to. The list is weighted with more likely runestones at the top.")
    public static List<String> availableDestinations = new ArrayList<>(Arrays.asList(
        "strange:spawn_point",
        "strange:stone_circle",
        "minecraft:village",
        "minecraft:pillager_outpost",
        "minecraft:desert_pyramid",
        "minecraft:jungle_temple",
        "minecraft:mineshaft",
        "minecraft:ocean_ruin",
        "minecraft:swamp_hut",
        "minecraft:igloo",
        "strange:underground_ruin",
        "quark:big_dungeon"
    ));

    @ObjectHolder("strange:runestone")
    public static TileEntityType<RunestoneTileEntity> tile;

    @CapabilityInject(IRunestonesCapability.class)
    public static final Capability<IRunestonesCapability> RUNESTONES = null;

    @Override
    public void init() {
        for (int i = 0; i < numberOfRunes; i++) {
            runestones.add(new RunestoneBlock(this, i));
        }

        tile = TileEntityType.Builder.create(RunestoneTileEntity::new).build(null);
        RegistryHandler.registerTile(tile, new ResourceLocation(Strange.MOD_ID, "runestone"));
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event) {
        CapabilityManager.INSTANCE.register(IRunestonesCapability.class, new RunestonesStorage(), RunestonesCapability::new);
    }

    @Override
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        ordered = new ArrayList<>();
        int j = 0;

        for (int i = 0; i < numberOfRunes; i++) {
            String dest = availableDestinations.get(j);
            ResourceLocation res = new ResourceLocation(dest);
            float weight = 1.0F - (j / (float) (numberOfRunes + 10));
            boolean addStructure;

            if (res.equals(RunestoneHelper.SPAWN)) {
                addStructure = true;
            } else {
                Feature<?> value = ForgeRegistries.FEATURES.getValue(res);
                addStructure = value != null;
            }

            if (!addStructure) {
                Strange.LOG.warn("Could not find registered structure " + dest + ", ignoring as runestone destination.");
                ordered.add(new Destination(RunestoneHelper.SPAWN, weight));
            } else {
                ordered.add(new Destination(res, weight));
            }

            if (j++ >= availableDestinations.size() - 1)
                j = 0;
        }

        Strange.LOG.debug(ordered.toString());
    }

    @Override
    public void onServerStarted(FMLServerStartedEvent event) {
        long seed = event.getServer().getWorld(DimensionType.OVERWORLD).getSeed();
        Random rand = new Random();
        rand.setSeed(seed);

        destinations = new ArrayList<>(ordered);
        Collections.shuffle(destinations, rand);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPearlImpact(ProjectileImpactEvent event) {
        if (!event.getEntity().world.isRemote
            && event.getEntity() instanceof EnderPearlEntity
            && event.getRayTraceResult().getType() == RayTraceResult.Type.BLOCK
            && ((ProjectileItemEntity) event.getEntity()).getThrower() instanceof PlayerEntity
        ) {
            PlayerEntity player = (PlayerEntity) ((ProjectileItemEntity) event.getEntity()).getThrower();
            if (player == null) return; // to prevent null inspection issues below

            World world = event.getEntity().world;
            BlockPos pos = ((BlockRayTraceResult) event.getRayTraceResult()).getPos();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block instanceof BaseRunestoneBlock) { // remove ender pearl if it's a runestone collision
                event.setCanceled(true);
                event.getEntity().remove();
            }

            if (block instanceof RunestoneBlock) {
                boolean success = prepareTeleport((ServerWorld)world, pos, player);

                if (success)
                    effectActivate(world, pos);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        if (event.phase == Phase.END
            && event.player.world.getGameTime() % INTERVAL == 0
            && !event.player.world.isRemote
            && !destinations.isEmpty()
        ) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.player;
            ServerWorld world = (ServerWorld) player.world;

            checkLookingAtRune(world, player);
            checkTeleport(world, player);
        }
    }

    // show message when looking at a runestone.  See Entity.java:1433
    private void checkLookingAtRune(ServerWorld world, ServerPlayerEntity player) {
        Vec3d vec3d = player.getEyePosition(1.0F);
        Vec3d vec3d1 = player.getLook(1.0F);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * 6, vec3d1.y * 6, vec3d1.z * 6);

        BlockRayTraceResult result = world.rayTraceBlocks(new RayTraceContext(vec3d, vec3d2, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));
        BlockPos runePos = result.getPos();
        BlockState lookedAt = world.getBlockState(runePos);

        if (lookedAt.getBlock() instanceof RunestoneBlock) {
            RunestoneBlock block = (RunestoneBlock) lookedAt.getBlock();
            if (runestones.contains(block)) {
                TranslationTextComponent message;
                IRunestonesCapability cap = Runestones.getCapability(player);
                int rune = getRuneValue(block);

                if (cap.getDiscoveredTypes().contains(rune)) {
                    Destination dest = destinations.get(rune);

                    // try and format the string into something nice
                    String desc = dest.structure.getPath();
                    desc = desc.replaceAll("_", " ");
                    desc = StringHelper.capitalizeString(desc);

                    BlockPos destPos = dest.isSpawnPoint() ? world.getSpawnPoint() : cap.getDestination(runePos);

                    if (destPos != null) {
                        effectTravelled(world, runePos);
                        message = new TranslationTextComponent("runestone.strange.rune_travelled", desc, destPos.getX(), destPos.getZ());
                    } else {
                        effectDiscovered(world, runePos);
                        message = new TranslationTextComponent("runestone.strange.rune_connects", desc);
                    }
                } else {
                    message = new TranslationTextComponent("runestone.strange.rune_unknown");
                }

                player.sendStatusMessage(message, true);
            }
        }
    }

    private void checkTeleport(ServerWorld world, ServerPlayerEntity player) {
        if (!playerTeleportRunestone.isEmpty() && playerTeleportRunestone.containsKey(player.getUniqueID())) {
            UUID id = player.getUniqueID();
            BlockPos pos = playerTeleportRunestone.get(id);
            doTeleport(world, pos, player);
            playerTeleportRunestone.remove(id);
        }
    }

    @SubscribeEvent
    public void onAttachCaps(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof PlayerEntity)) return;
        event.addCapability(Runestones.RUNESTONES_CAP_ID, new RunestonesProvider());
    }

    @SubscribeEvent
    public void onPlayerSave(PlayerEvent.SaveToFile event) {
        event.getPlayer().getPersistentData().put(
            Runestones.RUNESTONES_CAP_ID.toString(),
            Runestones.getCapability(event.getPlayer()).writeNBT()
        );
    }

    @SubscribeEvent
    public void onPlayerLoad(PlayerEvent.LoadFromFile event) {
        Runestones.getCapability(event.getPlayer()).readNBT(
            event.getPlayer().getPersistentData()
                .get(Runestones.RUNESTONES_CAP_ID.toString())
        );
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Runestones.getCapability(event.getPlayer()).readNBT(
            event.getPlayer().getPersistentData()
                .get(Runestones.RUNESTONES_CAP_ID.toString())
        );
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        event.getPlayer().getPersistentData().put(
            Runestones.RUNESTONES_CAP_ID.toString(),
            Runestones.getCapability(event.getPlayer()).writeNBT()
        );
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        IRunestonesCapability oldCap = Runestones.getCapability(event.getOriginal());
        IRunestonesCapability newCap = Runestones.getCapability(event.getPlayer());
        newCap.readNBT(oldCap.writeNBT());
    }

    @SuppressWarnings("ALL") // what
    public static IRunestonesCapability getCapability(PlayerEntity player) {
        return player.getCapability(RUNESTONES, null).orElse(new DummyCapability());
    }

    public static BlockState getRunestoneBlock(int runeValue) {
        return runestones.get(runeValue).getDefaultState();
    }

    public static int getRuneValue(BaseRunestoneBlock block) {
        return block.getRuneValue();
    }

    public static BlockState getRandomBlock(BlockPos pos) {
        return getRunestoneBlock(new Random(pos.toLong()).nextInt(destinations.size()));
    }

    public static BlockPos getInnerPos(World world, Random rand) {
        return Outerlands.getInnerPos(world, rand);
    }

    public static BlockPos getOuterPos(World world, Random rand) {
        return Outerlands.getOuterPos(world, rand);
    }

    private int getRuneValue(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof RunestoneBlock)) return -1;
        int rune = getRuneValue((RunestoneBlock) state.getBlock());
        return rune;
    }

    @Nullable
    private BlockPos getRuneDestination(ServerWorld world, BlockPos pos) {
        int runeValue = getRuneValue(world, pos);

        Random rand = world.rand;
        rand.setSeed(pos.toLong());

        Destination destination = destinations.get(runeValue);
        return destination.getAndRecordDestination(world, pos, rand);
    }

    private boolean prepareTeleport(ServerWorld world, BlockPos pos, PlayerEntity player) {
        BlockState state = world.getBlockState(pos);
        BlockPos destPos = getRuneDestination(world, pos);

        if (destPos == null) {
            RunestoneHelper.runeError(world, pos, true, player);
            return false;
        }

        int rune = getRuneValue(world, pos);
        if (rune == -1) {
            Strange.LOG.warn("Failed to get the value of the rune.");
            RunestoneHelper.runeError(world, pos, true, player);
            return false;
        }

        // force remote chunk
        boolean result = LocationHelper.addForcedChunk(world, destPos);
        if (!result) {
            Strange.LOG.warn("Could not load destination chunk, giving up.");
            RunestoneHelper.runeError(world, pos, true, player);
            return false;
        }

        // store discovered rune against player
        CriteriaTriggers.ENTER_BLOCK.trigger((ServerPlayerEntity) player, state);
        Runestones.getCapability(player).discoverType(rune);

        // record the destination for the player
        Runestones.getCapability(player).recordDestination(pos, destPos);

        // prep player for teleport
        playerTeleportRunestone.put(player.getUniqueID(), destPos);

        return true;
    }

    private void doTeleport(ServerWorld world, BlockPos destPos, PlayerEntity player) {
        int duration = protectionDuration * 20;
        player.addPotionEffect(new EffectInstance(Effects.SLOW_FALLING, duration, 2));
        player.addPotionEffect(new EffectInstance(Effects.RESISTANCE, duration, 2));

        PlayerHelper.teleportSurface(player, destPos, 0, p1 -> {
            final BlockPos playerPos = p1.getPosition();
            p1.setPositionAndUpdate(playerPos.getX(), playerPos.getY() + 2, playerPos.getZ());

            // unload the chunk!
            LocationHelper.removeForcedChunk(world, destPos);
        });
    }

    public static void effectActivate(World world, BlockPos pos) {
        double spread = 0.75D;
        double px = pos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
        double py = pos.getY() + 0.5D + (Math.random() - 0.5D) * spread;
        double pz = pos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
        ((ServerWorld) world).spawnParticle(ParticleTypes.PORTAL, px, py, pz, 10, 0.3D, 0.3D, 0.3D, 0.3D);
        world.playSound(null, pos, StrangeSounds.RUNESTONE_TRAVEL, SoundCategory.PLAYERS, 0.6F, 1.05F);
    }

    public static void effectTravelled(World world, BlockPos pos) {
        double spread = 1.5D;
        double px = pos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
        double py = pos.getY() + 0.5D + (Math.random() - 0.5D) * spread;
        double pz = pos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
        ((ServerWorld) world).spawnParticle(ParticleTypes.ENCHANT, px, py, pz, 10, 0.2D, 0.6D, 0.2D, 0.45D);
    }

    public static void effectDiscovered(World world, BlockPos pos) {
        double spread = 1.75D;
        if (world.rand.nextFloat() < 0.6F) {
            double px = pos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
            double py = pos.getY() + 0.5D + (Math.random() - 0.5D) * spread;
            double pz = pos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
            ((ServerWorld) world).spawnParticle(ParticleTypes.ENCHANT, px, py, pz, 3, 0.15D, 0.3D, 0.15D, 0.2D);
        }
    }
}
