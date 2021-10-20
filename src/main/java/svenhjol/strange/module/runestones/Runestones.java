package svenhjol.strange.module.runestones;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.helper.StringHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.runestones.enums.IRunestoneMaterial;
import svenhjol.strange.module.runestones.enums.RunestoneMaterial;
import svenhjol.strange.module.runestones.location.BaseLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@CommonModule(mod = Strange.MOD_ID)
public class Runestones extends CharmModule {
    public static final int TELEPORT_TICKS = 10;
    public static final int TIERS = 5;
    public static final int MAX_ITEMS = 3; // number of item possibilities
    public static final int SHOW_TEXT_CLUE = 5; // show text clue when there are this many (or less) unknowns

    public static final ResourceLocation MSG_CLIENT_SET_ACTIVE_DESTINATION = new ResourceLocation(Strange.MOD_ID, "client_set_looking_at");
    public static final ResourceLocation BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "runestone");
    public static final ResourceLocation RUNESTONE_DUST_ID = new ResourceLocation(Strange.MOD_ID, "runestone_dust");

    public static Map<IRunestoneMaterial, RunestoneBlock> RUNESTONE_BLOCKS = new HashMap<>();
    public static BlockEntityType<RunestoneBlockEntity> BLOCK_ENTITY;
    public static RunestoneDustItem RUNESTONE_DUST;
    public static EntityType<RunestoneDustEntity> RUNESTONE_DUST_ENTITY;
    public static MenuType<RunestoneMenu> MENU;

    public static Map<ResourceLocation, List<BaseLocation>> DIMENSION_LOCATIONS = new HashMap<>();

    public static Map<UUID, BlockPos> teleportFrom = new HashMap<>(); // location of the runestone that the player activated
    public static Map<UUID, BlockPos> teleportTo = new HashMap<>(); // location to teleport player who has just activated
    public static Map<UUID, Integer> teleportTicks = new HashMap<>(); // number of ticks since player has activated

    public static Map<Integer, List<Item>> items = new HashMap<>();
    public static Map<ResourceLocation, List<String>> clues = new HashMap<>();

    @Config(name = "Travel distance in blocks", description = "Maximum number of blocks that you will be teleported via a runestone.")
    public static int maxDistance = 5000;

    @Config(name = "Travel protection time", description = "Number of seconds of regeneration, fire resistance and slow fall after teleporting.")
    public static int protectionDuration = 10;

    @Config(name = "Travel penalty time", description = "Number of seconds of poison, wither, burning, slowness or weakness after teleporting without the correct item.")
    public static int penaltyDuration = 10;

    @Config(name = "Locations", description = "Locations that runestones may teleport you to.  Format: [dimension -> location]\nLocations are ordered by rune difficulty. Missing structures will be skipped.")
    public static List<String> configLocations = new ArrayList<>(Arrays.asList(
        "overworld -> strange:stone_circle",
        "overworld -> village",
        "overworld -> pillager_outpost",
        "overworld -> desert_pyramid",
        "overworld -> mineshaft",
        "overworld -> badlands",
        "overworld -> bamboo_jungle",
        "overworld -> jungle_pyramid",
        "overworld -> flower_forest",
        "overworld -> ice_spikes",
        "overworld -> snowy_slopes",
        "overworld -> windswept_savanna",
        "overworld -> ocean_ruin",
        "overworld -> swamp_hut",
        "overworld -> igloo",
        "overworld -> meadow",
        "overworld -> ruined_portal",
        "overworld -> lush_caves",
        "overworld -> dripstone_caves",
        "overworld -> jagged_peaks",
        "overworld -> shipwreck",
        "overworld -> strange:surface_ruin",
        "overworld -> strange:cave_ruin",
        "overworld -> strange:deep_ruin",
        "overworld -> monument",
        "overworld -> mushroom_fields",
        "overworld -> woodland_mansion",
        "overworld -> stronghold",
        "the_nether -> strange:stone_circle",
        "the_nether -> crimson_forest",
        "the_nether -> ruined_portal",
        "the_nether -> soul_sand_valley",
        "the_nether -> warped_forest",
        "the_nether -> basalt_deltas",
        "the_nether -> bastion_remnant",
        "the_nether -> fortress",
        "the_nether -> strange:nether_ruin",
        "the_nether -> strange:citadel",
        "the_end -> strange:stone_circle",
        "the_end -> endcity",
        "the_end -> strange:end_ruin",
        "strange:darkland -> strange:stone_circle",
        "strange:darkland -> strange:surface_ruin",
        "strange:darkland -> strange:cave_ruin",
        "strange:darkland -> strange:deep_ruin"
    ));

    @Config(name = "Clues", description = "Clues give hints about the kind of structure or biome that a runestone will teleport you to.")
    public static List<String> configClues = new ArrayList<>(Arrays.asList(
        "strange:spawn_point    -> safe",
        "strange:stone_circle   -> surface,safe,ancient",
        "village                -> surface,populated,safe,lucrative",
        "pillager_outpost       -> surface,populated,dangerous",
        "desert_pyramid         -> surface,lucrative,dry",
        "jungle_pyramid         -> surface,lucrative,humid",
        "mineshaft              -> underground,dangerous",
        "ocean_ruin             -> underwater,dangerous,lucrative,ancient",
        "swamp_hut              -> surface,dangerous",
        "igloo                  -> surface,safe,cold",
        "ruined_portal          -> lucrative,ancient",
        "shipwreck              -> lucrative,underwater",
        "strange:surface_ruin   -> surface,ancient",
        "strange:cave_ruin      -> underground,dangerous,lucrative,populated",
        "strange:deep_ruin      -> underground,lucrative,ancient",
        "monument               -> underwater,surface,populated,dangerous,lucrative",
        "stronghold             -> underground,dangerous,lucrative",
        "bastion_remnant        -> populated,dangerous,lucrative,ancient",
        "fortress               -> populated,dangerous,lucrative",
        "strange:nether_ruin    -> underground,lucrative",
        "strange:citadel        -> populated,dangerous,lucrative",
        "endcity                -> surface,populated,dangerous,lucrative",
        "strange:end_ruin       -> dangerous,lucrative",
        "badlands               -> surface,dry",
        "bamboo_jungle          -> surface,humid",
        "flower_forest          -> surface,safe",
        "ice_spikes             -> surface,cold",
        "snowy_slopes           -> surface,cold",
        "windswept_savanna      -> surface,dry",
        "meadow                 -> surface,safe",
        "lush_caves             -> underground,humid",
        "dripstone_caves        -> underground,humid",
        "jagged_peaks           -> surface,cold",
        "mushroom_fields        -> surface,lucrative",
        "crimson_forest         -> surface",
        "warped_forest          -> surface",
        "soul_sand_valley       -> surface,dangerous",
        "basalt_deltas          -> surface,dangerous"
    ));

    @Override
    public void register() {
        for (IRunestoneMaterial material : RunestoneMaterial.getTypes()) {
            RUNESTONE_BLOCKS.put(material, new RunestoneBlock(this, material));
        }

        // TODO: Dispenser behavior. See Strange16 RunicAltars

        BLOCK_ENTITY = RegistryHelper.blockEntity(BLOCK_ID, RunestoneBlockEntity::new);
        MENU = RegistryHelper.screenHandler(BLOCK_ID, RunestoneMenu::new);

        // setup runestone dust item and entity
        RUNESTONE_DUST = new RunestoneDustItem(this);
        RUNESTONE_DUST_ENTITY = RegistryHelper.entity(RUNESTONE_DUST_ID, FabricEntityTypeBuilder
            .<RunestoneDustEntity>create(MobCategory.MISC, RunestoneDustEntity::new)
            .trackRangeBlocks(80)
            .trackedUpdateRate(10)
            .dimensions(EntityDimensions.fixed(2.0F, 2.0F)));
    }

    @Override
    public void runWhenEnabled() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::handleServerStarted);
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        RunestoneLoot.create();
        RunestoneLocations.init();
    }

    private void handleWorldLoad(MinecraftServer server, Level level) {
        if (level.dimension() == Level.OVERWORLD) {
            this.initClues(server);
        }
    }

    private void handleServerStarted(MinecraftServer server) {
    }

    private void initClues(MinecraftServer server) {
        clues = new HashMap<>();

        configClues.forEach(str -> {
            List<String> split = StringHelper.splitConfigEntry(str);
            if (split.size() != 2) return;

            ResourceLocation locationId = new ResourceLocation(split.get(0));
            List<String> cluesList = Arrays.stream(split.get(1).split(",")).map(s -> s.toLowerCase(Locale.ROOT).trim()).collect(Collectors.toList());
            clues.put(locationId, cluesList);
        });

        Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
        Resource resource;
        try {
            resource = server.getResourceManager().getResource(RunestoneLoot.REQUIRED_ITEMS);
        } catch (IOException e) {
            return;
        }

        InputStream inputStream = resource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        JsonObject jsonObject = GsonHelper.fromJson(gson, reader, JsonObject.class);
        JsonArray pools = GsonHelper.getAsJsonArray(jsonObject, "pools");

        for (int p = 0; p < pools.size(); p++) {
            JsonArray elements = GsonHelper.getAsJsonArray((JsonObject) pools.get(p), "entries");
            for (int e = 0; e < elements.size(); e++) {
                JsonObject entry = (JsonObject)elements.get(e);
                String name = entry.get("name").getAsString();
                ResourceLocation res = new ResourceLocation(name);

                // try instantiate
                if (Registry.ITEM.getOptional(res).isPresent()) {
                    items.computeIfAbsent(p, a -> new ArrayList<>()).add(Registry.ITEM.get(res));
                } else {
                    LogHelper.debug(this.getClass(), "Could not find item in registry: " + res);
                }
            }
        }
    }
}
