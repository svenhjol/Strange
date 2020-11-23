package svenhjol.strange.runestones;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.charm.base.helper.PosHelper;
import svenhjol.charm.base.helper.WorldHelper;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.charm.event.PlayerLoadDataCallback;
import svenhjol.charm.event.PlayerSaveDataCallback;
import svenhjol.charm.event.PlayerTickCallback;
import svenhjol.charm.event.ThrownEntityImpactCallback;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.base.helper.RunestoneHelper;
import svenhjol.strange.runestones.destination.BiomeDestination;
import svenhjol.strange.runestones.destination.Destination;
import svenhjol.strange.runestones.destination.StructureDestination;

import java.io.File;
import java.util.*;

import static svenhjol.strange.base.helper.RunestoneHelper.NUMBER_OF_RUNES;

@Module(mod = Strange.MOD_ID, client = RunestonesClient.class, description = "Fast travel to points of interest in your world by using an Ender Pearl.")
public class Runestones extends CharmModule {
    public static final Identifier ID = new Identifier(Strange.MOD_ID, "runestone");
    public static final Identifier MSG_CLIENT_SYNC_LEARNED = new Identifier(Strange.MOD_ID, "client_sync_learned");
    public static final String LEARNED_TAG = "learned";
    public static final int TELEPORT_TICKS = 10;

    public static final List<RunestoneBlock> RUNESTONE_BLOCKS = new ArrayList<>();
    public static BlockEntityType<RunestoneBlockEntity> BLOCK_ENTITY;

    public static List<Destination> availableDestinations = new ArrayList<>(); // pool of possible destinations, may populate before loadWorldEvent
    public static List<Destination> worldDestinations = new ArrayList<>(); // destinations shuffled according to world seed

    public static Map<UUID, BlockPos> teleportFrom = new HashMap<>(); // location of the runestone that the player hit with the pearl
    public static Map<UUID, BlockPos> teleportTo = new HashMap<>(); // location to teleport player who has just used pearl
    public static Map<UUID, Integer> teleportTicks = new HashMap<>(); // number of ticks since player has used pearl

    @Config(name = "Travel distance in blocks", description = "Maximum number of blocks that you will be teleported via a runestone.")
    public static int maxDistance = 4000;

    @Config(name = "Travel protection time", description = "Number of seconds of regeneration and slow-fall when teleporting through a runestone.")
    public static int protectionDuration = 10;

    @Config(name = "Available structures", description = "Structures that runestones may teleport you to. The list is weighted with more likely structures at the top.")
    public static List<String> configStructures = new ArrayList<>(Arrays.asList(
        "strange:spawn_point",
        "strange:stone_circle",
        "minecraft:village",
        "minecraft:pillager_outpost",
        "minecraft:desert_pyramid",
        "minecraft:jungle_pyramid",
        "minecraft:mineshaft",
        "minecraft:ocean_ruin",
        "minecraft:swamp_hut",
        "minecraft:igloo",
        "minecraft:ruined_portal"
    ));

    @Config(name = "Available biomes", description = "Biomes that runestones may teleport you to. The list is weighted with more likely biomes at the top.")
    public static List<String> configBiomes = new ArrayList<>(Arrays.asList(
        "minecraft:desert_lakes",
        "minecraft:mountain_edge",
        "minecraft:giant_tree_taiga",
        "minecraft:badlands",
        "minecraft:bamboo_jungle",
        "minecraft:ice_spikes"
    ));

    @Override
    public void register() {
        for (int i = 0; i < NUMBER_OF_RUNES; i++) {
            RUNESTONE_BLOCKS.add(new RunestoneBlock(this, i));
        }

        BLOCK_ENTITY = RegistryHandler.blockEntity(ID, RunestoneBlockEntity::new);
    }

    @Override
    public void init() {
        // write player learned runes to disk
        PlayerSaveDataCallback.EVENT.register(((player, playerDataDir) -> {
            List<Integer> runes = RunestoneHelper.getLearnedRunes(player);
            CompoundTag tag = new CompoundTag();
            tag.putIntArray(LEARNED_TAG, runes);
            PlayerSaveDataCallback.writeFile(new File(playerDataDir, player.getUuidAsString() + "_runestones.dat"), tag);
        }));

        // load player learned runes from disk
        PlayerLoadDataCallback.EVENT.register(((player, playerDataDir) -> {
            CompoundTag tag = PlayerLoadDataCallback.readFile(new File(playerDataDir, player.getUuidAsString() + "_runestones.dat"));
            if (tag.contains(LEARNED_TAG)) {
                RunestoneHelper.resetLearnedRunes(player);

                int[] intArray = tag.getIntArray(LEARNED_TAG);
                for (int rune : intArray) {
                    RunestoneHelper.addLearnedRune(player, rune);
                }
                Charm.LOG.debug("Loaded player rune learned data");
            }
        }));

        // listen for thrown enderpearls
        ThrownEntityImpactCallback.EVENT.register(this::tryEnderPearlImpact);

        // listen for player ticks
        PlayerTickCallback.EVENT.register(player -> {
            if (player.world.isClient || availableDestinations.isEmpty())
                return;

            tryLookingAtRunestone((ServerPlayerEntity)player);
            tryTeleport((ServerPlayerEntity)player);
        });

        initDestinations();
    }

    /**
     * Use the loadWorld event to shuffle the destinations according to the world seed.
     * @param server {@link MinecraftServer}
     */
    @Override
    public void loadWorld(MinecraftServer server) {
        ServerWorld overworld = server.getWorld(World.OVERWORLD);

        if (overworld == null) {
            Charm.LOG.warn("Cannot access overworld, unable to get seed.");
            return;
        }

        long seed = overworld.getSeed();
        Random random = new Random();
        random.setSeed(seed);

        worldDestinations = new ArrayList<>(availableDestinations);
        Collections.shuffle(worldDestinations, random);
    }

    private void initDestinations() {
        // setup the list of runestone destination structures
        int r = 0;
        while (r < NUMBER_OF_RUNES) {
            for (int j = 0; j < configStructures.size(); j++) {
                if (r >= NUMBER_OF_RUNES) break;
                String configStructure = configStructures.get(j);
                Identifier locationId = new Identifier(configStructure);

                float weight = 1.0F - (j / (float) (NUMBER_OF_RUNES + 10));
                boolean addStructure = locationId.equals(RunestoneHelper.SPAWN) || Registry.STRUCTURE_FEATURE.get(locationId) != null;

                if (addStructure) {
                    availableDestinations.add(new StructureDestination(locationId, weight));
                } else {
                    Charm.LOG.warn("Could not find registered structure " + configStructure + ", ignoring as runestone destination");
                    availableDestinations.add(new StructureDestination(RunestoneHelper.SPAWN, weight));
                }
                r++;
            }

            for (int j = 0; j < configBiomes.size(); j++) {
                if (r >= NUMBER_OF_RUNES) break;
                String configBiome = configBiomes.get(j);
                Identifier locationId = new Identifier(configBiome);

                float weight = 1.0F - (j / (float) (NUMBER_OF_RUNES + 10));
                boolean addBiome = BuiltinRegistries.BIOME.get(locationId) != null;

                if (addBiome) {
                    availableDestinations.add(new BiomeDestination(locationId, weight));
                } else {
                    Charm.LOG.warn("Could not find registered biome " + configBiome + ", ignoring as runestone destination");
                    availableDestinations.add(new BiomeDestination(RunestoneHelper.SPAWN, weight));
                }
                r++;
            }
        }
    }

    private void tryLookingAtRunestone(ServerPlayerEntity player) {
        // don't check this every single tick
        if (player.world.getTime() % 10 > 0)
            return;

        Vec3d cameraPosVec = player.getCameraPosVec(1.0F);
        Vec3d rotationVec = player.getRotationVec(1.0F);
        Vec3d vec3d = cameraPosVec.add(rotationVec.x * 6, rotationVec.y * 6, rotationVec.z * 6);

        World world = player.world;
        BlockHitResult raycast = world.raycast(new RaycastContext(cameraPosVec, vec3d, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
        BlockPos runePos = raycast.getBlockPos();
        BlockState lookedAt = world.getBlockState(runePos);

        if (lookedAt.getBlock() instanceof RunestoneBlock) {
            RunestoneBlock block = (RunestoneBlock)lookedAt.getBlock();
            if (RUNESTONE_BLOCKS.contains(block)) {
                TranslatableText message = new TranslatableText("runestone.strange.unknown");
                int runeValue = getRuneValue((ServerWorld) world, runePos);

                if (RunestoneHelper.hasLearnedRune(player, runeValue)) {
                    Destination destination = worldDestinations.get(runeValue);
                    message = new TranslatableText("runestone.strange.known", RunestoneHelper.getFormattedLocationName(destination.getLocation()));
                }

                BlockEntity blockEntity = world.getBlockEntity(runePos);
                if (blockEntity instanceof RunestoneBlockEntity) {
                    RunestoneBlockEntity runestone = (RunestoneBlockEntity)blockEntity;

                    if (runestone.location != null) {
                        String formattedLocationName = RunestoneHelper.getFormattedLocationName(runestone.location);
                        if (runestone.player != null && !runestone.player.isEmpty()) {
                            message = new TranslatableText("runestone.strange.discovered_by", formattedLocationName, runestone.player);
                        } else {
                            message = new TranslatableText("runestone.strange.discovered", formattedLocationName);
                        }
                    }
                }

                player.sendMessage(message, true);
            }
        }
    }

    private void tryTeleport(ServerPlayerEntity player) {
        if (teleportTo.isEmpty())
            return;

        UUID uid = player.getUuid();

        if (teleportTicks.containsKey(uid)) {
            int ticks = teleportTicks.get(uid);
            BlockPos src = teleportFrom.get(uid);
            BlockPos dest = teleportTo.get(uid);

            // force load the remote chunk for smoother teleport
            if (ticks == TELEPORT_TICKS) {
                if (!WorldHelper.addForcedChunk((ServerWorld)player.world, dest)) {
                    Charm.LOG.warn("Could not load destination chunk, giving up");
                    RunestoneHelper.explode(player.world, src, player, true);
                    clearTeleport(player);
                    return;
                }
            }

            if (ticks > 0) {
                teleportTicks.put(uid, --ticks);
                return;
            }

            if (!teleportTo.containsKey(uid))
                return;

            BlockPos destPos = teleportTo.get(uid);
            World world = player.world;
            BlockPos surfacePos = PosHelper.getSurfacePos(world, destPos);

            // don't need the player's teleport info any more
            clearTeleport(player);

            int duration = protectionDuration * 20;
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, duration, 2));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, duration, 2));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, duration, 2));

            if (surfacePos != null) // should never be null but check anyway
                player.teleport(surfacePos.getX(), surfacePos.getY(), surfacePos.getZ());

            // must unload the chunk once finished
            WorldHelper.removeForcedChunk((ServerWorld)player.world, destPos);
        }
    }

    private ActionResult tryEnderPearlImpact(ThrownEntity entity, HitResult hitResult) {
        if (!entity.world.isClient
            && entity instanceof EnderPearlEntity
            && hitResult.getType() == HitResult.Type.BLOCK
            && entity.getOwner() instanceof PlayerEntity
        ) {
            PlayerEntity player = (PlayerEntity)entity.getOwner();
            if (player == null)
                return ActionResult.PASS;

            World world = entity.world;
            BlockPos pos = ((BlockHitResult)hitResult).getBlockPos();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block instanceof RunestoneBlock) {
                entity.remove();

                boolean result = onPlayerActivateRunestone((ServerWorld)world, pos, (ServerPlayerEntity)player);
                if (result) {
                    world.playSound(null, pos, StrangeSounds.RUNESTONE_TRAVEL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }
            }
        }

        return ActionResult.PASS;
    }

    private boolean onPlayerActivateRunestone(ServerWorld world, BlockPos runePos, ServerPlayerEntity player) {
        int runeValue = getRuneValue(world, runePos);
        if (runeValue == -1) {
            Charm.LOG.warn("Failed to get the value of the rune at " + runePos.toShortString());
            return RunestoneHelper.explode(world, runePos, player, true);
        }

        Random random = new Random();
        random.setSeed(runePos.toImmutable().asLong());
        BlockPos destPos = worldDestinations.get(runeValue).getDestination(world, runePos, maxDistance, random, player);

        if (destPos == null)
            return RunestoneHelper.explode(world, runePos, player, true);

        UUID uid = player.getUuid();

        // prep for teleport
        teleportFrom.put(uid, runePos);
        teleportTo.put(uid, destPos);
        teleportTicks.put(uid, TELEPORT_TICKS);

        RunestoneHelper.addLearnedRune(player, runeValue);
        return true;
    }

    private int getRuneValue(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof RunestoneBlock)) return -1;
        return ((RunestoneBlock)state.getBlock()).getRuneValue();
    }

    private void clearTeleport(PlayerEntity player) {
        UUID uid = player.getUuid();
        teleportTicks.remove(uid);
        teleportTo.remove(uid);
        teleportFrom.remove(uid);
    }
}
