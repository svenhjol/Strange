package svenhjol.strange.module.runestones;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.runestones.enums.IRunestoneMaterial;
import svenhjol.strange.module.runestones.enums.RunestoneMaterial;

import java.util.*;

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
        "overworld -> mineshaft",
        "overworld -> desert_pyramid",
        "overworld -> jungle_pyramid",
        "overworld -> swamp_hut",
        "overworld -> flower_forest",
        "overworld -> savanna",
        "overworld -> windswept_savanna",
        "overworld -> ice_spikes",
        "overworld -> snowy_slopes",
        "overworld -> igloo",
        "overworld -> ocean_ruin",
        "overworld -> meadow",
        "overworld -> ruined_portal",
        "overworld -> lush_caves",
        "overworld -> dripstone_caves",
        "overworld -> jagged_peaks",
        "overworld -> shipwreck",
        "overworld -> strange:ruin",
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
        "strange:mirror -> strange:stone_circle",
        "strange:floating_islands -> village",
        "strange:floating_islands -> pillager_outpost",
        "strange:floating_islands -> desert_pyramid"
    ));

    @Override
    public void register() {
        for (IRunestoneMaterial material : RunestoneMaterial.getTypes()) {
            RUNESTONE_BLOCKS.put(material, new RunestoneBlock(this, material));
        }

        // TODO: Dispenser behavior. See Strange16 RunicAltars

        BLOCK_ENTITY = CommonRegistry.blockEntity(BLOCK_ID, RunestoneBlockEntity::new);
        MENU = CommonRegistry.menu(BLOCK_ID, RunestoneMenu::new);

        // setup runestone dust item and entity
        RUNESTONE_DUST = new RunestoneDustItem(this);
        RUNESTONE_DUST_ENTITY = CommonRegistry.entity(RUNESTONE_DUST_ID, FabricEntityTypeBuilder
            .<RunestoneDustEntity>create(MobCategory.MISC, RunestoneDustEntity::new)
            .trackRangeBlocks(80)
            .trackedUpdateRate(10)
            .dimensions(EntityDimensions.fixed(2.0F, 2.0F)));
    }

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        RunestoneLocations.init();
    }

    private void handleWorldLoad(MinecraftServer server, Level level) {
        if (level.dimension() == Level.OVERWORLD) {
            RunestoneClues.init(server);
        }
    }
}
