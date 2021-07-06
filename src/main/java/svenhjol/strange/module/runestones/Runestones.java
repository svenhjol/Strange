package svenhjol.strange.module.runestones;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.event.*;
import svenhjol.charm.helper.*;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.init.StrangeSounds;
import svenhjol.strange.module.runestones.destination.BaseDestination;
import svenhjol.strange.module.runestones.destination.BiomeDestination;
import svenhjol.strange.module.runestones.destination.StructureDestination;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

import static svenhjol.strange.module.runestones.RunestonesHelper.NUMBER_OF_RUNES;

@CommonModule(mod = Strange.MOD_ID, description = "Fast travel to points of interest in your world by using an ender pearl.")
public class Runestones extends CharmModule {
    public static final ResourceLocation BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "runestone");
    public static final ResourceLocation RUNESTONE_DUST_ID = new ResourceLocation(Strange.MOD_ID, "runestone_dust");
    public static final ResourceLocation MSG_CLIENT_CACHE_LEARNED_RUNES = new ResourceLocation(Strange.MOD_ID, "client_cache_learned_runes");
    public static final ResourceLocation MSG_CLIENT_CACHE_DESTINATION_NAMES = new ResourceLocation(Strange.MOD_ID, "client_cache_destination_names");
    public static final ResourceLocation TRIGGER_ACTIVATED_RUNESTONE = new ResourceLocation(Strange.MOD_ID, "activated_runestone");
    public static final ResourceLocation TRIGGER_LEARNED_ALL_RUNES = new ResourceLocation(Strange.MOD_ID, "learned_all_runes");

    public static final String LEARNED_NBT = "learned";
    public static final int TELEPORT_TICKS = 10;

    public static List<RunestoneBlock> RUNESTONE_BLOCKS = new ArrayList<>();
    public static BlockEntityType<RunestoneBlockEntity> BLOCK_ENTITY;
    public static RunestoneDustItem RUNESTONE_DUST;
    public static EntityType<RunestoneDustEntity> RUNESTONE_DUST_ENTITY;

    public static final ResourceLocation RUNE_PLATE_LOOT_ID = new ResourceLocation(Strange.MOD_ID, "rune_plate_loot");
    public static LootItemFunctionType RUNE_PLATE_LOOT_FUNCTION;

    public static Map<Integer, RunePlateItem> RUNE_PLATES = new HashMap<>();
    public static List<BaseDestination> AVAILABLE_DESTINATIONS = new ArrayList<>(); // pool of possible destinations, may populate before loadWorldEvent
    public static List<BaseDestination> WORLD_DESTINATIONS = new ArrayList<>(); // destinations shuffled according to world seed

    public static int SPAWN_RUNE = -1; // value of a rune that links back to spawn point

    public static Map<UUID, BlockPos> teleportFrom = new HashMap<>(); // location of the runestone that the player hit with the pearl
    public static Map<UUID, BlockPos> teleportTo = new HashMap<>(); // location to teleport player who has just used pearl
    public static Map<UUID, Integer> teleportTicks = new HashMap<>(); // number of ticks since player has used pearl

    @Config(name = "Travel distance in blocks", description = "Maximum number of blocks that you will be teleported via a runestone.")
    public static int maxDistance = 4000;

    @Config(name = "Travel protection time", description = "Number of seconds of regeneration and slow-fall when teleporting through a runestone.")
    public static int protectionDuration = 10;

    @Config(name = "Learn from other players", description = "If true, looking at a runestone whose location has already been discovered by another player lets you immediately learn that rune.")
    public static boolean learnFromOtherPlayers = true;

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
        "minecraft:ruined_portal",
        "strange:cave_ruin",
        "strange:surface_ruin",
        "strange:deep_ruin"
    ));

    @Config(name = "Available biomes", description = "Biomes that runestones may teleport you to. The list is weighted with more likely biomes at the top.")
    public static List<String> configBiomes = new ArrayList<>(Arrays.asList(
        "minecraft:flower_forest",
        "minecraft:ice_spikes",
        "minecraft:badlands"
    ));

    @Override
    public void register() {
        for (int i = 0; i < NUMBER_OF_RUNES; i++) {
            RUNESTONE_BLOCKS.add(new RunestoneBlock(this, i));
            RUNE_PLATES.put(i, new RunePlateItem(this, i));
        }

        BLOCK_ENTITY = RegistryHelper.blockEntity(BLOCK_ID, RunestoneBlockEntity::new);

        // setup runestone dust item and entity
        RUNESTONE_DUST = new RunestoneDustItem(this);
        RUNESTONE_DUST_ENTITY = RegistryHelper.entity(RUNESTONE_DUST_ID, FabricEntityTypeBuilder
            .<RunestoneDustEntity>create(MobCategory.MISC, RunestoneDustEntity::new)
            .trackRangeBlocks(80)
            .trackedUpdateRate(10)
            .dimensions(EntityDimensions.fixed(2.0F, 2.0F)));

        // TODO: phase2 loot table
        RUNE_PLATE_LOOT_FUNCTION = RegistryHelper.lootFunctionType(RUNE_PLATE_LOOT_ID, new LootItemFunctionType(new RunePlateLootFunction.Serializer()));
    }

    @Override
    public void runWhenEnabled() {
        // write player learned runes to disk
        PlayerSaveDataCallback.EVENT.register(this::handlePlayerSave);

        // load player learned runes from disk
        PlayerLoadDataCallback.EVENT.register(this::handlePlayerLoad);

        // listen for thrown enderpearls
        ThrownEntityImpactCallback.EVENT.register(this::tryEnderPearlImpact);

        // listen for player ticks
        PlayerTickCallback.EVENT.register(this::handlePlayerTick);

        // listen for broken runestones
        PlayerBlockBreakEvents.BEFORE.register(this::handleBlockBreak);

        // use loadworld event to shuffle the runestones according to world seed
        LoadServerFinishCallback.EVENT.register(this::handleLoadWorld);

        initDestinations();
    }

    /**
     * Use the loadWorld event to shuffle the destinations according to the world seed.
     */
    private void handleLoadWorld(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);

        if (overworld == null) {
            LogHelper.warn(this.getClass(), "Cannot access overworld, unable to get seed.");
            return;
        }

        long seed = overworld.getSeed();
        Random random = new Random();
        random.setSeed(seed);

        WORLD_DESTINATIONS = new ArrayList<>(AVAILABLE_DESTINATIONS);
        Collections.shuffle(WORLD_DESTINATIONS, random);

        SPAWN_RUNE = -1;
        for (int i = 0; i < WORLD_DESTINATIONS.size(); i++) {
            if (WORLD_DESTINATIONS.get(i).isSpawnPoint()) {
                SPAWN_RUNE = i;
                break;
            }
        }
    }

    public static void sendLearnedRunesPacket(ServerPlayer player) {
        List<Integer> learnedRunes = RunestonesHelper.getLearnedRunes(player);
        int[] learned = learnedRunes.stream().mapToInt(i -> i).toArray();

        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeVarIntArray(learned);
        ServerPlayNetworking.send(player, MSG_CLIENT_CACHE_LEARNED_RUNES, data);
    }

    public static void sendDestinationNamesPacket(ServerPlayer player) {
        List<Integer> learnedRunes = RunestonesHelper.getLearnedRunes(player);

        CompoundTag outTag = new CompoundTag();

        for (int rune : learnedRunes) {
            BaseDestination destination = WORLD_DESTINATIONS.get(rune);
            String name = RunestonesHelper.getFormattedLocationName(destination.getLocation());
            outTag.putString(String.valueOf(rune), name);
        }

        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeNbt(outTag);
        ServerPlayNetworking.send(player, MSG_CLIENT_CACHE_DESTINATION_NAMES, data);
    }

    private void initDestinations() {
        int r = 0;
        while (r < NUMBER_OF_RUNES) {
            for (int j = 0; j < configStructures.size(); j++) {
                if (r >= NUMBER_OF_RUNES) break;
                String configStructure = configStructures.get(j);
                ResourceLocation locationId = new ResourceLocation(configStructure);

                float weight = 1.0F - (j / (float)NUMBER_OF_RUNES);
                boolean isSpawnRune = locationId.equals(RunestonesHelper.SPAWN);
                boolean addStructure = isSpawnRune || Registry.STRUCTURE_FEATURE.get(locationId) != null;

                if (addStructure) {
                    AVAILABLE_DESTINATIONS.add(new StructureDestination(locationId, weight));
                } else {
                    LogHelper.warn(this.getClass(), "Could not find registered structure " + configStructure + ", ignoring as runestone destination");
                    AVAILABLE_DESTINATIONS.add(new StructureDestination(RunestonesHelper.SPAWN, weight));
                }

                r++;
            }

            for (int j = 0; j < configBiomes.size(); j++) {
                if (r >= NUMBER_OF_RUNES) break;
                String configBiome = configBiomes.get(j);
                ResourceLocation locationId = new ResourceLocation(configBiome);

                float weight = 1.0F - (j / (float)NUMBER_OF_RUNES);
                boolean addBiome = BuiltinRegistries.BIOME.get(locationId) != null;

                if (addBiome) {
                    AVAILABLE_DESTINATIONS.add(new BiomeDestination(locationId, weight));
                } else {
                    LogHelper.warn(this.getClass(), "Could not find registered biome " + configBiome + ", ignoring as runestone destination");
                    AVAILABLE_DESTINATIONS.add(new BiomeDestination(RunestonesHelper.SPAWN, weight));
                }
                r++;
            }
        }
    }

    private void tryLookingAtRunestone(ServerPlayer player) {
        // don't check this every single tick
        if (player.level.getGameTime() % 10 > 0)
            return;

        Vec3 cameraPosVec = player.getEyePosition(1.0F);
        Vec3 rotationVec = player.getViewVector(1.0F);
        Vec3 vec3d = cameraPosVec.add(rotationVec.x * 6, rotationVec.y * 6, rotationVec.z * 6);

        Level world = player.level;
        BlockHitResult raycast = world.clip(new ClipContext(cameraPosVec, vec3d, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        BlockPos runePos = raycast.getBlockPos();
        BlockState lookedAt = world.getBlockState(runePos);

        if (lookedAt.getBlock() instanceof RunestoneBlock block) {
            if (RUNESTONE_BLOCKS.contains(block)) {
                TranslatableComponent message = new TranslatableComponent("runestone.strange.unknown");
                int runeValue = getRuneValue((ServerLevel) world, runePos);

                if (RunestonesHelper.hasLearnedRune(player, runeValue)) {
                    BaseDestination destination = WORLD_DESTINATIONS.get(runeValue);
                    message = new TranslatableComponent("runestone.strange.known", RunestonesHelper.getFormattedLocationName(destination.getLocation()));
                }

                BlockEntity blockEntity = world.getBlockEntity(runePos);
                if (blockEntity instanceof RunestoneBlockEntity runestone) {
                    if (runestone.location != null) {
                        String formattedLocationName = RunestonesHelper.getFormattedLocationName(runestone.location);
                        if (runestone.player != null && !runestone.player.isEmpty()) {
                            message = new TranslatableComponent("runestone.strange.discovered_by", formattedLocationName, runestone.player);
                        } else {
                            message = new TranslatableComponent("runestone.strange.discovered", formattedLocationName);
                        }

                        if (learnFromOtherPlayers && !RunestonesHelper.hasLearnedRune(player, runeValue)) {
                            world.playSound(null, runePos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
                            RunestonesHelper.addLearnedRune(player, runeValue);
                        }
                    }
                }

                player.displayClientMessage(message, true);
            }
        }
    }

    private void tryTeleport(ServerPlayer player) {
        if (teleportTo.isEmpty())
            return;

        UUID uid = player.getUUID();
        player.stopRiding();

        if (teleportTicks.containsKey(uid)) {
            int ticks = teleportTicks.get(uid);
            BlockPos src = teleportFrom.get(uid);
            BlockPos dest = teleportTo.get(uid);

            // force load the remote chunk for smoother teleport
            if (ticks == TELEPORT_TICKS) {
                if (!WorldHelper.addForcedChunk((ServerLevel)player.level, dest)) {
                    LogHelper.warn(this.getClass(), "Could not load destination chunk, giving up");
                    RunestonesHelper.explode(player.level, src, player, true);
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
            Level world = player.level;
            BlockPos surfacePos = PosHelper.getSurfacePos(world, destPos);

            // don't need the player's teleport info any more
            clearTeleport(player);

            int duration = protectionDuration * 20;
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, duration, 2));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 2));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, duration, 2));

            if (surfacePos != null) // should never be null but check anyway
                player.teleportToWithTicket(surfacePos.getX(), surfacePos.getY(), surfacePos.getZ());

            // must unload the chunk once finished
            WorldHelper.removeForcedChunk((ServerLevel)player.level, destPos);
        }
    }

    private InteractionResult tryEnderPearlImpact(ThrowableProjectile entity, HitResult hitResult) {
        if (!entity.level.isClientSide
            && entity instanceof ThrownEnderpearl
            && hitResult.getType() == HitResult.Type.BLOCK
            && entity.getOwner() instanceof Player player
        ) {
            Level world = entity.level;
            BlockPos pos = ((BlockHitResult)hitResult).getBlockPos();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block instanceof RunestoneBlock) {
                entity.discard();

                boolean result = onPlayerActivateRunestone((ServerLevel)world, pos, (ServerPlayer)player);
                if (result) {
                    world.playSound(null, pos, StrangeSounds.RUNESTONE_TRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }

    private boolean onPlayerActivateRunestone(ServerLevel world, BlockPos runePos, ServerPlayer player) {
        int runeValue = getRuneValue(world, runePos);
        if (runeValue == -1) {
            LogHelper.warn(this.getClass(), "Failed to get the value of the rune at " + runePos.toString());
            return RunestonesHelper.explode(world, runePos, player, true);
        }

        Random random = new Random();
        random.setSeed(runePos.immutable().asLong());
        BlockPos destPos = WORLD_DESTINATIONS.get(runeValue).getDestination(world, runePos, maxDistance, random, player);

        if (destPos == null)
            return RunestonesHelper.explode(world, runePos, player, true);

        UUID uuid = player.getUUID();

        // prep for teleport
        teleportFrom.put(uuid, runePos);
        teleportTo.put(uuid, destPos);
        teleportTicks.put(uuid, TELEPORT_TICKS);

        triggerActivatedRunestone(player);
        RunestonesHelper.addLearnedRune(player, runeValue);
        return true;
    }

    private int getRuneValue(ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof RunestoneBlock)) return -1;
        return ((RunestoneBlock)state.getBlock()).getRuneValue();
    }

    private void clearTeleport(Player player) {
        UUID uid = player.getUUID();
        teleportTicks.remove(uid);
        teleportTo.remove(uid);
        teleportFrom.remove(uid);
    }

    private void handlePlayerSave(Player player, File playerDataDir) {
        List<Integer> runes = RunestonesHelper.getLearnedRunes(player);
        CompoundTag tag = new CompoundTag();
        tag.putIntArray(LEARNED_NBT, runes);
        PlayerSaveDataCallback.writeFile(new File(playerDataDir, player.getStringUUID() + "_strange_runestones.dat"), tag);
    }

    private void handlePlayerLoad(Player player, File playerDataDir) {
        CompoundTag tag = PlayerLoadDataCallback.readFile(new File(playerDataDir, player.getStringUUID() + "_strange_runestones.dat"));
        if (tag.contains(LEARNED_NBT)) {
            RunestonesHelper.resetLearnedRunes(player);

            int[] intArray = tag.getIntArray(LEARNED_NBT);
            for (int rune : intArray) {
                RunestonesHelper.addLearnedRune(player, rune);
            }
            LogHelper.debug(this.getClass(), "Loaded player rune learned data");
        }
    }

    /**
     * Ticks the player to check if certain actions need to be taken:
     * - if the player is looking at a runestone
     * - if the player is scheduled to be teleported
     */
    private void handlePlayerTick(Player player) {
        if (player.level.isClientSide || AVAILABLE_DESTINATIONS.isEmpty())
            return;

        ServerPlayer serverPlayer = (ServerPlayer)player;
        tryLookingAtRunestone(serverPlayer);
        tryTeleport(serverPlayer);
    }

    /**
     * Called just before a runestone block is broken.
     * This is where we do drops for runestone dust and plates.
     */
    private boolean handleBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        if (!(state.getBlock() instanceof RunestoneBlock))
            return true;

        int runeValue = getRuneValue((ServerLevel) world, pos);
        if (runeValue >= 0) {
            int drops = 1 + world.random.nextInt((EnchantmentsHelper.getFortune(player) * 2) + 2);
            for (int i = 0; i < drops; i++) {
                world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(RUNESTONE_DUST)));
            }

            float luck = player.getLuck();
            int fortune = EnchantmentsHelper.getFortune(player);
            boolean shouldDropPlate = (luck * 3) + fortune + world.random.nextInt(3) > 3;
            int maxPlateDrops = Math.max(1, Math.min(3, world.random.nextInt(Math.max(1, (int)luck + fortune))));

            if (shouldDropPlate) {
                for (int i = 0; i < maxPlateDrops; i++) {
                    world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(RUNE_PLATES.get(runeValue))));
                }
                RunestonesHelper.explode(world, pos, null, false);
            }
        }

        return true; // always allow runestone to be broken
    }

    public static void triggerActivatedRunestone(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_ACTIVATED_RUNESTONE);
    }

    public static void triggerLearnedAllRunes(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_LEARNED_ALL_RUNES);
    }
}
