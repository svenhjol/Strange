package svenhjol.strange.module.runestones;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.api.event.AddRunestoneDestinationCallback;
import svenhjol.strange.module.runes.Tier;
import svenhjol.strange.module.runestones.definition.CluesDefinition;
import svenhjol.strange.module.runestones.definition.DestinationDefinition;
import svenhjol.strange.module.runestones.definition.ItemDefinition;

import java.util.*;
import java.util.stream.Collectors;

@CommonModule(mod = Strange.MOD_ID, priority = 5)
public class Runestones extends CharmModule {
    public static final int MAX_ITEMS = 3; // number of item possibilities
    public static final int SHOW_TEXT_CLUE = 5; // show text clue when there are this many (or less) unknowns

    public static final String RUNESTONES_FOLDER = "runestones";
    public static final String DESTINATIONS_DEFINITION_FOLDER = "runestones/destinations";
    public static final String ITEMS_DEFINITION_FOLDER = "runestones/items";

    public static final ResourceLocation BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "runestone");
    public static final ResourceLocation RUNESTONE_DUST_ID = new ResourceLocation(Strange.MOD_ID, "runestone_dust");
    public static final ResourceLocation SPAWN = new ResourceLocation(Strange.MOD_ID, "spawn_point");
    public static final String UNKNOWN_CLUE = "unknown";

    public static Map<RunestoneMaterial, RunestoneBlock> RUNESTONE_BLOCKS = new HashMap<>();
    public static BlockEntityType<RunestoneBlockEntity> BLOCK_ENTITY;
    public static EntityType<RunestoneDustEntity> RUNESTONE_DUST_ENTITY;
    public static RunestoneDustItem RUNESTONE_DUST;
    public static MenuType<RunestoneMenu> MENU;

    public static Map<ResourceLocation, LinkedList<ResourceLocation>> DESTINATIONS = new HashMap<>();
    public static Map<ResourceLocation, Map<Tier, List<Item>>> ITEMS = new TreeMap<>();
    public static Map<ResourceLocation, List<String>> CLUES = new HashMap<>();
    public static List<Item> DEFAULT_ITEMS;

    @Override
    public void register() {
        for (var material : RunestoneMaterial.getTypes()) {
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
    }

    private void handleWorldLoad(MinecraftServer server, Level level) {
        ResourceManager manager = server.getResourceManager();
        setupItems(manager, level);
        setupDestinations(manager, level);

        if (level.dimension() == Level.OVERWORLD) {
            setupClues(manager);
        }
    }

    private void setupItems(ResourceManager manager, Level level) {
        ResourceLocation dimension = level.dimension().location();
        Collection<ResourceLocation> resources = manager.listResources(ITEMS_DEFINITION_FOLDER, f -> f.endsWith(".json"));
        ITEMS.computeIfAbsent(dimension, m -> new HashMap<>());
        ITEMS.get(dimension).clear();

        for (ResourceLocation resource : resources) {
            String path = resource.getPath();
            if (path.endsWith(dimension.getNamespace() + "/" + dimension.getPath() + ".json")) {
                try {
                    ItemDefinition definition = ItemDefinition.deserialize(manager.getResource(resource));

                    for (Tier tier : Tier.values()) {
                        List<Item> items = definition.get(tier).stream()
                            .map(ResourceLocation::new)
                            .map(Registry.ITEM::get)
                            .collect(Collectors.toList());

                        List<Item> entries = ITEMS.get(dimension).computeIfAbsent(tier, a -> new ArrayList<>());
                        entries.addAll(items);
                    }

                } catch (Exception e) {
                    LogHelper.warn(getClass(), "Could not load runestone destinations definition from " + resource + ": " + e.getMessage());
                }
            }
        }
    }

    private void setupClues(ResourceManager manager) {
        Collection<ResourceLocation> resources = manager.listResources(RUNESTONES_FOLDER, f -> f.endsWith(".json"));
        ResourceLocation resource = null;
        CLUES.clear();

        BuiltinRegistries.BIOME.keySet().forEach(r -> CLUES.computeIfAbsent(r, a -> new ArrayList<>()).add("biome"));
        Registry.STRUCTURE_FEATURE.keySet().forEach(r -> CLUES.computeIfAbsent(r, a -> new ArrayList<>()).add("structure"));

        for (ResourceLocation r : resources) {
            if (r.getPath().endsWith("clues.json")) {
                resource = r;
                break;
            }
        }

        if (resource != null) {
            try {
                CluesDefinition definition = CluesDefinition.deserialize(manager.getResource(resource));
                Map<ResourceLocation, List<String>> clues = definition.getAllClues();
                for (Map.Entry<ResourceLocation, List<String>> entry : clues.entrySet()) {
                    ResourceLocation key = entry.getKey();
                    List<String> value = entry.getValue();
                    if (CLUES.containsKey(key)) {
                        CLUES.get(key).addAll(value);
                    } else {
                        CLUES.put(key, value);
                    }
                }
            } catch (Exception e) {
                LogHelper.warn(getClass(), "Could not load runestone clues definition from " + resource + ": " + e.getMessage());
            }
        }
    }

    private void setupDestinations(ResourceManager manager, Level level) {
        ResourceLocation dimension = level.dimension().location();
        Collection<ResourceLocation> resources = manager.listResources(DESTINATIONS_DEFINITION_FOLDER, f -> f.endsWith(".json"));
        DESTINATIONS.computeIfAbsent(dimension, a -> new LinkedList<>());
        DESTINATIONS.get(dimension).clear();

        for (ResourceLocation resource : resources) {
            String path = resource.getPath();
            if (path.endsWith(dimension.getNamespace() + "/" + dimension.getPath() + ".json")) {
                try {
                    DestinationDefinition definition = DestinationDefinition.deserialize(manager.getResource(resource));
                    List<ResourceLocation> destinations = definition.getDestinations().stream()
                        .map(ResourceLocation::new)
                        .filter(r -> WorldHelper.isStructure(r) || WorldHelper.isBiome(r))
                        .collect(Collectors.toList());

                    LinkedList<ResourceLocation> entries = DESTINATIONS.get(dimension);
                    for (int i = 0; i < destinations.size(); i++) {
                        ResourceLocation destination = destinations.get(i);
                        if (entries.contains(destination)) continue;
                        int ii = i * 2;
                        if (ii < entries.size()) {
                            entries.add(ii, destination);
                        } else {
                            entries.add(destination);
                        }
                    }

                } catch (Exception e) {
                    LogHelper.warn(getClass(), "Could not load runestone destinations definition from " + resource + ": " + e.getMessage());
                }
            }
        }

        // Allow other modules to add their custom destinations
        AddRunestoneDestinationCallback.EVENT.invoker().interact(level, DESTINATIONS.get(dimension));
    }

    static {
        DEFAULT_ITEMS = List.of(
            Items.ENDER_PEARL,
            Items.OBSIDIAN,
            Items.IRON_BLOCK,
            Items.GOLD_BLOCK,
            Items.DIAMOND,
            Items.GOLD_INGOT,
            Items.IRON_INGOT
        );
    }
}
