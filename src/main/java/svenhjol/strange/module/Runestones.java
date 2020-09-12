package svenhjol.strange.module;

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
import net.minecraft.util.registry.Registry;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.feature.StructureFeature;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.event.PlayerTickCallback;
import svenhjol.meson.event.ThrownEntityImpactCallback;
import svenhjol.meson.helper.StringHelper;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.block.RunestoneBlock;
import svenhjol.strange.blockentity.RunestoneBlockEntity;
import svenhjol.strange.event.PlayerLoadDataCallback;
import svenhjol.strange.event.PlayerSaveDataCallback;
import svenhjol.strange.helper.RunestoneHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

@Module(description = "Runestones allow fast travel to points of interest in your world by using an Ender Pearl.")
public class Runestones extends MesonModule {
    public static final Identifier ID = new Identifier(Strange.MOD_ID, "runestone");
    public static final String DISCOVERIES_TAG = "discoveries";

    public static final List<RunestoneBlock> RUNESTONE_BLOCKS = new ArrayList<>();
    public static BlockEntityType<RunestoneBlockEntity> BLOCK_ENTITY;

    public static List<Destination> destinations = new ArrayList<>();
    public static Map<UUID, BlockPos> playerTeleportPos = new HashMap<>();
    public static Map<UUID, Integer> playerTeleportTicks = new HashMap<>();
    public static Map<UUID, List<Integer>> playerDiscoveries = new HashMap<>();

    private static final int numberOfRunes = 26;

    @Config(name = "Travel distance in blocks", description = "Maximum number of blocks that you will be transported from a runestone.")
    public static int maxDistance = 4000;

    @Config(name = "Travel protection time", description = "Number of seconds of regeneration and slow-fall when travelling through a runestone.")
    public static int protectionDuration = 10;

    @Config(name = "Available destinations", description = "Destinations that runestones may teleport you to. The list is weighted with more likely runestones at the top.")
    public static List<String> availableDestinations = new ArrayList<>(Arrays.asList(
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
        "strange:underground_ruin"
    ));

    @Override
    public void register() {
        for (int i = 0; i < numberOfRunes; i++) {
            RUNESTONE_BLOCKS.add(new RunestoneBlock(this, i));
        }

        BLOCK_ENTITY = BlockEntityType.Builder.create(RunestoneBlockEntity::new).build(null);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, ID, BLOCK_ENTITY);
    }

    @Override
    public void init() {
        // write player discovered runes to disk
        PlayerSaveDataCallback.EVENT.register(((player, playerDataDir) -> {
            UUID uid = player.getUuid();
            if (playerDiscoveries.containsKey(uid)) {
                List<Integer> runes = playerDiscoveries.get(uid);
                CompoundTag tag = new CompoundTag();
                tag.putIntArray(DISCOVERIES_TAG, runes);
                PlayerSaveDataCallback.writeFile(new File(playerDataDir, player.getUuidAsString() + "_runestones.dat"), tag);
            }
        }));

        PlayerLoadDataCallback.EVENT.register(((player, playerDataDir) -> {
            UUID uid = player.getUuid();
            CompoundTag tag = PlayerLoadDataCallback.readFile(new File(playerDataDir, player.getUuidAsString() + "_runestones.dat"));
            if (tag.contains(DISCOVERIES_TAG)) {
                playerDiscoveries.remove(uid);
                playerDiscoveries.put(uid, new ArrayList<>());

                int[] intArray = tag.getIntArray(DISCOVERIES_TAG);
                for (int i : intArray) {
                    playerDiscoveries.get(uid).add(i);
                }
                Meson.LOG.debug("Loaded player rune discovery data");
            }
        }));

        // listen for thrown enderpearls
        ThrownEntityImpactCallback.EVENT.register(this::tryEnderPearlImpact);

        // listen for player ticks
        PlayerTickCallback.EVENT.register(player -> {
            if (player.world.isClient || destinations.isEmpty())
                return;

            tryLookingAtRunestone((ServerPlayerEntity)player);
            tryTeleport((ServerPlayerEntity)player);
        });

        // setup the list of runestone destination structures
        int j = 0;
        for (int i = 0; i < numberOfRunes; i++) {
            String dest = availableDestinations.get(j);
            Identifier destId = new Identifier(dest);

            float weight = 1.0F - (j / (float)(numberOfRunes + 10));
            boolean addStructure = destId.equals(RunestoneHelper.SPAWN) || StructureFeature.STRUCTURES.containsKey(destId.getPath());

            if (addStructure) {
                destinations.add(new Destination(destId, weight));
            } else {
                Meson.LOG.warn("Could not find registered structure " + dest + ", ignoring as runestone destination");
                destinations.add(new Destination(RunestoneHelper.SPAWN, weight));
            }

            if (j++ >= availableDestinations.size() - 1)
                j = 0;
        }

        Meson.LOG.debug(destinations.toString());
    }

    /**
     * Use the loadWorld event to shuffle the destinations according to the world seed.
     * @param server {@link MinecraftServer}
     */
    @Override
    public void loadWorld(MinecraftServer server) {
        ServerWorld overworld = server.getWorld(World.OVERWORLD);

        if (overworld == null) {
            Meson.LOG.warn("Cannot access overworld, unable to get seed.");
            return;
        }

        long seed = overworld.getSeed();
        Random random = new Random();
        random.setSeed(seed);
        Collections.shuffle(destinations, random);
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

                // TODO player knowledge of the rune

                BlockEntity blockEntity = world.getBlockEntity(runePos);
                if (blockEntity instanceof RunestoneBlockEntity) {
                    RunestoneBlockEntity runestone = (RunestoneBlockEntity)blockEntity;

                    if (runestone.description != null)
                        message = new TranslatableText("runestone.strange.discovered", runestone.description);
                }

                player.sendMessage(message, true);
            }
        }
    }

    private void tryTeleport(ServerPlayerEntity player) {
        if (playerTeleportPos.isEmpty())
            return;

        UUID uid = player.getUuid();

        if (playerTeleportTicks.containsKey(uid)) {
            int ticks = playerTeleportTicks.get(uid);
            if (ticks > 0) {
                playerTeleportTicks.put(uid, --ticks);
                return;
            }

            if (!playerTeleportPos.containsKey(uid))
                return;

            BlockPos destPos = playerTeleportPos.get(uid);
            playerTeleportTicks.remove(uid);
            playerTeleportPos.remove(uid);

            World world = player.world;
            int surface = 0;

            for (int y = world.getHeight(); y >= 0; --y) {
                BlockPos n = new BlockPos(destPos.getX(), y, destPos.getZ());
                if (world.isAir(n) && !world.isAir(n.down())) {
                    surface = y;
                    break;
                }
            }

            if (surface <= 0) {
                Meson.LOG.warn("Failed to find a surface value to spawn the player");
                return;
            }

            int duration = protectionDuration * 20;
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, duration, 2));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, duration, 2));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, duration, 2));

            player.teleport(destPos.getX(), surface, destPos.getZ());
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

                boolean result = onPearlImpact((ServerWorld)world, pos, (ServerPlayerEntity)player);
                if (result) {
                    world.playSound(null, pos, StrangeSounds.RUNESTONE_TRAVEL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }
            }
        }

        return ActionResult.PASS;
    }

    private boolean onPearlImpact(ServerWorld world, BlockPos runePos, ServerPlayerEntity player) {
        int runeValue = getRuneValue(world, runePos);
        if (runeValue == -1) {
            Meson.LOG.warn("Failed to get the value of the rune at " + runePos.toShortString());
            return RunestoneHelper.explode(world, runePos, player, true);
        }

        Random random = new Random();
        random.setSeed(runePos.toImmutable().asLong());
        BlockPos destPos = destinations.get(runeValue).getDestination(world, runePos, random);

        if (destPos == null) {
            return RunestoneHelper.explode(world, runePos, player, true);
        }

        // force remote chunk
        boolean result = WorldHelper.addForcedChunk(world, destPos);
        if (!result) {
            Meson.LOG.warn("Could not load destination chunk, giving up");
            return RunestoneHelper.explode(world, runePos, player, true);
        }

        UUID uid = player.getUuid();

        if (!playerDiscoveries.containsKey(uid))
            playerDiscoveries.put(uid, new ArrayList<>());

        if (!playerDiscoveries.get(uid).contains(runeValue))
            playerDiscoveries.get(uid).add(runeValue);

        // prep for teleport
        playerTeleportPos.put(uid, destPos);
        playerTeleportTicks.put(uid, 10);

        return true;
    }

    private int getRuneValue(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof RunestoneBlock)) return -1;
        return ((RunestoneBlock)state.getBlock()).getRuneValue();
    }

    static class Destination {
        private Identifier structure;
        private float weight;

        public Destination(Identifier structure, float weight) {
            this.structure = structure;
            this.weight = weight;
        }

        public boolean isSpawnPoint() {
            return this.structure.equals(RunestoneHelper.SPAWN);
        }

        @Nullable
        public BlockPos getDestination(ServerWorld world, BlockPos runePos, Random random) {
            int maxDistance = Runestones.maxDistance;

            BlockEntity blockEntity = world.getBlockEntity(runePos);
            if (blockEntity instanceof RunestoneBlockEntity) {
                RunestoneBlockEntity runeBlockEntity = (RunestoneBlockEntity)blockEntity;
                BlockPos position = runeBlockEntity.position;
                String structure = runeBlockEntity.structure;

                if (structure != null && !structure.isEmpty() && position != null) {
                    Meson.LOG.debug("Found destination in runestone: " + structure);
                    return position;
                }
            }

            // bounds check
            WorldBorder border = world.getWorldBorder();
            int xdist = -maxDistance + random.nextInt(maxDistance *2);
            int zdist = -maxDistance + random.nextInt(maxDistance *2);
            BlockPos destPos = runePos.add(xdist, 0, zdist);

            if (destPos.getX() > border.getBoundEast())
                destPos = new BlockPos(border.getBoundEast(), destPos.getY(), destPos.getZ());

            if (destPos.getX() < border.getBoundWest())
                destPos = new BlockPos(border.getBoundWest(), destPos.getY(), destPos.getZ());

            if (destPos.getZ() > border.getBoundSouth())
                destPos = new BlockPos(destPos.getX(), destPos.getY(), border.getBoundSouth());

            if (destPos.getZ() < border.getBoundNorth())
                destPos = new BlockPos(destPos.getX(), destPos.getY(), border.getBoundNorth());

            BlockPos foundPos;

            if (isSpawnPoint()) {
                foundPos = world.getSpawnPos();
            } else {

                // TODO check this works with modded structures
                String structureId = structure.getNamespace().equals("minecraft") ? structure.getPath() : structure.toString();
                StructureFeature<?> structureFeature = StructureFeature.STRUCTURES.get(structureId);

                if (structureFeature == null) {
                    Meson.LOG.warn("Could not find structure in registry of type: " + structure);
                    return null;
                }

                Meson.LOG.debug("Trying to locate structure in the world: " + structure);
                foundPos = world.locateStructure(structureFeature, destPos, 1000, false);
            }

            if (foundPos == null) {
                Meson.LOG.warn("Could not locate structure: " + structure);
                return null;
            }

            foundPos = RunestoneHelper.addRandomOffset(foundPos, random, 8);

            if (blockEntity instanceof RunestoneBlockEntity) {
                RunestoneBlockEntity runeBlockEntity = (RunestoneBlockEntity)blockEntity;
                runeBlockEntity.position = foundPos;
                runeBlockEntity.structure = this.structure.toString();
                runeBlockEntity.description = StringHelper.capitalize(this.structure.getPath().replaceAll("_", " "));
                runeBlockEntity.markDirty();

                Meson.LOG.debug("Stored structure in runestone for next use");
            }

            return foundPos;
        }
    }
}
