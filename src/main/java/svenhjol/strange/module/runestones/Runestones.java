package svenhjol.strange.module.runestones;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.runestones.definition.DestinationDefinition;
import svenhjol.strange.module.runestones.enums.IRunestoneMaterial;
import svenhjol.strange.module.runestones.enums.RunestoneMaterial;
import svenhjol.strange.api.event.AddRunestoneDestinationCallback;

import java.util.*;
import java.util.stream.Collectors;

@CommonModule(mod = Strange.MOD_ID, priority = 5)
public class Runestones extends CharmModule {
    public static final int TIERS = 5;
    public static final int MAX_ITEMS = 3; // number of item possibilities
    public static final int SHOW_TEXT_CLUE = 5; // show text clue when there are this many (or less) unknowns
    public static final String DESTINATIONS_DEFINITION_FOLDER = "runestones/destinations";

    public static final ResourceLocation BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "runestone");
    public static final ResourceLocation RUNESTONE_DUST_ID = new ResourceLocation(Strange.MOD_ID, "runestone_dust");
    public static final ResourceLocation MSG_CLIENT_SET_DESTINATION = new ResourceLocation(Strange.MOD_ID, "client_set_destination");
    public static final ResourceLocation SPAWN = new ResourceLocation(Strange.MOD_ID, "spawn_point");
    public static final String UNKNOWN_CLUE = "unknown";

    public static Map<IRunestoneMaterial, RunestoneBlock> RUNESTONE_BLOCKS = new HashMap<>();
    public static BlockEntityType<RunestoneBlockEntity> BLOCK_ENTITY;
    public static EntityType<RunestoneDustEntity> RUNESTONE_DUST_ENTITY;
    public static RunestoneDustItem RUNESTONE_DUST;
    public static MenuType<RunestoneMenu> MENU;

    public static Map<ResourceLocation, LinkedList<ResourceLocation>> DESTINATIONS = new HashMap<>();
    public static Map<ResourceLocation, Map<Integer, List<Item>>> ITEMS = new TreeMap<>();
    public static Map<ResourceLocation, List<String>> CLUES = new HashMap<>();

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
    }

    private void handleWorldLoad(MinecraftServer server, Level level) {
        if (level.dimension() == Level.OVERWORLD) {
            RunestoneClues.init(server);
        }

        setupDestinations(server, level);
    }

    private void setupDestinations(MinecraftServer server, Level level) {
        ResourceLocation dimension = level.dimension().location();
        ResourceManager manager = server.getResourceManager();
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
}
