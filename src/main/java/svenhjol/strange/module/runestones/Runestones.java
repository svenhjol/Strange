package svenhjol.strange.module.runestones;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.helper.StringHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.helper.LootHelper;
import svenhjol.strange.module.runestones.enums.IRunestoneMaterial;
import svenhjol.strange.module.runestones.enums.RunestoneMaterial;

import java.util.*;
import java.util.stream.Collectors;

@CommonModule(mod = Strange.MOD_ID)
public class Runestones extends CharmModule {
    public static final int TIERS = 5;
    public static final int MAX_ITEMS = 3; // number of item possibilities
    public static final int SHOW_TEXT_CLUE = 5; // show text clue when there are this many (or less) unknowns

    public static final ResourceLocation BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "runestone");
    public static final ResourceLocation RUNESTONE_DUST_ID = new ResourceLocation(Strange.MOD_ID, "runestone_dust");
    public static final ResourceLocation MSG_CLIENT_SET_DESTINATION = new ResourceLocation(Strange.MOD_ID, "client_set_destination");

    public static Map<IRunestoneMaterial, RunestoneBlock> RUNESTONE_BLOCKS = new HashMap<>();
    public static BlockEntityType<RunestoneBlockEntity> BLOCK_ENTITY;
    public static EntityType<RunestoneDustEntity> RUNESTONE_DUST_ENTITY;
    public static RunestoneDustItem RUNESTONE_DUST;
    public static MenuType<RunestoneMenu> MENU;

    public static Map<ResourceLocation, LinkedList<ResourceLocation>> DESTINATIONS = new HashMap<>();
    public static Map<ResourceLocation, Map<Integer, List<Item>>> ITEMS = new TreeMap<>();
    public static Map<ResourceLocation, List<String>> CLUES = new HashMap<>();

    @Config(name = "Destinations", description = "Destinations that runestones may teleport you to.  Format: [dimension -> id]\nDestinations are ordered by rune difficulty. Missing structures will be skipped.")
    public static List<String> configDestinations = new ArrayList<>(Arrays.asList(
        "overworld -> strange:stone_circle",
        "overworld -> village",
        "overworld -> pillager_outpost",
        "overworld -> desert_pyramid",
        "overworld -> mineshaft",
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
        "overworld -> mansion",
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
        "the_end -> end_highlands",
        "the_end -> end_midlands",
        "the_end -> small_end_islands",
        "the_end -> endcity",
        "the_end -> strange:end_ruin",
        "strange:darkland -> strange:stone_circle",
        "strange:darkland -> strange:surface_ruin",
        "strange:darkland -> strange:cave_ruin",
        "strange:darkland -> strange:deep_ruin"
    ));

    @Config(name = "Clues", description = "Clues give hints about the destination that a runestone will teleport you to.")
    public static List<String> configClues = new ArrayList<>(Arrays.asList(
        "strange:spawn_point -> safe",
        "strange:stone_circle -> surface,safe,ancient",
        "village -> surface,populated,safe,lucrative",
        "pillager_outpost -> surface,populated,dangerous",
        "desert_pyramid -> surface,lucrative,dry",
        "jungle_pyramid -> surface,lucrative,humid",
        "mineshaft -> underground,dangerous",
        "ocean_ruin -> underwater,dangerous,lucrative,ancient",
        "swamp_hut -> surface,dangerous",
        "igloo -> surface,safe,cold",
        "ruined_portal -> lucrative,ancient",
        "shipwreck -> lucrative,underwater",
        "strange:surface_ruin -> surface,ancient",
        "strange:cave_ruin -> underground,dangerous,lucrative,populated",
        "strange:deep_ruin -> underground,lucrative,ancient",
        "monument -> underwater,surface,populated,dangerous,lucrative",
        "stronghold -> underground,dangerous,lucrative",
        "bastion_remnant -> populated,dangerous,lucrative,ancient",
        "fortress -> populated,dangerous,lucrative",
        "strange:nether_ruin -> underground,lucrative",
        "strange:citadel -> populated,dangerous,lucrative",
        "endcity -> surface,populated,dangerous,lucrative",
        "strange:end_ruin -> dangerous,lucrative",
        "badlands -> surface,dry",
        "bamboo_jungle -> surface,humid",
        "flower_forest -> surface,safe",
        "ice_spikes -> surface,cold,biome",
        "snowy_slopes -> surface,cold,biome",
        "windswept_savanna -> surface,dry,biome",
        "meadow -> surface,safe,biome",
        "lush_caves -> underground,humid,biome",
        "dripstone_caves -> underground,humid,biome",
        "jagged_peaks -> surface,cold,biome",
        "mushroom_fields -> surface,lucrative,biome",
        "crimson_forest -> surface,biome",
        "warped_forest -> surface,biome",
        "soul_sand_valley -> surface,dangerous,biome",
        "basalt_deltas -> surface,dangerous,biome",
        "end_highlands -> surface,biome",
        "end_midlands -> surface,biome",
        "small_end_islands -> surface,biome"
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
        CLUES = new HashMap<>();
        ITEMS = new TreeMap<>();

        configClues.forEach(str -> {
            List<String> split = StringHelper.splitConfigEntry(str);
            if (split.size() != 2) return;

            ResourceLocation destinationId = new ResourceLocation(split.get(0));
            List<String> cluesList = Arrays.stream(split.get(1).split(",")).map(s -> s.toLowerCase(Locale.ROOT).trim()).collect(Collectors.toList());
            CLUES.put(destinationId, cluesList);
        });

        LootHelper.fetchItems(server, RunestoneLoot.OVERWORLD_ITEMS)
            .ifPresent(m -> ITEMS.put(ServerLevel.OVERWORLD.location(), m));

        LootHelper.fetchItems(server, RunestoneLoot.NETHER_ITEMS)
            .ifPresent(m -> ITEMS.put(ServerLevel.NETHER.location(), m));

        LootHelper.fetchItems(server, RunestoneLoot.END_ITEMS)
            .ifPresent(m -> ITEMS.put(ServerLevel.END.location(), m));
    }
}
