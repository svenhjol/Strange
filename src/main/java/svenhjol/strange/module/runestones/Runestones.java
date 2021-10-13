package svenhjol.strange.module.runestones;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.runestones.location.BaseLocation;
import svenhjol.strange.module.runestones.enums.IRunestoneMaterial;
import svenhjol.strange.module.runestones.enums.RunestoneMaterial;

import java.util.*;

@CommonModule(mod = Strange.MOD_ID)
public class Runestones extends CharmModule {
    public static final int TELEPORT_TICKS = 10;

    public static final ResourceLocation MSG_CLIENT_SET_ACTIVE_DESTINATION = new ResourceLocation(Strange.MOD_ID, "client_set_looking_at");

    public static final ResourceLocation BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "runestone");
    public static final ResourceLocation RUNESTONE_DUST_ID = new ResourceLocation(Strange.MOD_ID, "runestone_dust");

    public static Map<IRunestoneMaterial, RunestoneBlock> RUNESTONE_BLOCKS = new HashMap<>();
    public static BlockEntityType<RunestoneBlockEntity> BLOCK_ENTITY;
    public static RunestoneDustItem RUNESTONE_DUST;
    public static EntityType<RunestoneDustEntity> RUNESTONE_DUST_ENTITY;
    public static MenuType<RunestoneMenu> MENU;

    public static Map<ResourceLocation, List<BaseLocation>> AVAILABLE_LOCATIONS = new HashMap<>();

    public static Map<UUID, BlockPos> teleportFrom = new HashMap<>(); // location of the runestone that the player activated
    public static Map<UUID, BlockPos> teleportTo = new HashMap<>(); // location to teleport player who has just activated
    public static Map<UUID, Integer> teleportTicks = new HashMap<>(); // number of ticks since player has activated

    @Config(name = "Travel distance in blocks", description = "Maximum number of blocks that you will be teleported via a runestone.")
    public static int maxDistance = 5000;

    @Config(name = "Travel protection time", description = "Number of seconds of regeneration, fire resistance and slow fall after teleporting.")
    public static int protectionDuration = 10;

    @Config(name = "Structures", description = "Structures that runestones may teleport you to.  Format: [dimension -> structure]\nStructures are listed in order of rarity and missing structures will be skipped.")
    public static List<String> configStructures = new ArrayList<>(Arrays.asList(
        "minecraft:overworld -> strange:stone_circle",
        "minecraft:overworld -> minecraft:village",
        "minecraft:overworld -> minecraft:pillager_outpost",
        "minecraft:overworld -> minecraft:desert_pyramid",
        "minecraft:overworld -> minecraft:jungle_pyramid",
        "minecraft:overworld -> minecraft:mineshaft",
        "minecraft:overworld -> minecraft:ocean_ruin",
        "minecraft:overworld -> minecraft:swamp_hut",
        "minecraft:overworld -> minecraft:igloo",
        "minecraft:overworld -> minecraft:ruined_portal",
        "minecraft:overworld -> strange:cave_ruin",
        "minecraft:overworld -> strange:surface_ruin",
        "minecraft:overworld -> strange:deep_ruin",
        "minecraft:the_nether -> strange:stone_circle",
        "minecraft:the_nether -> minecraft:ruined_portal",
        "minecraft:the_nether -> minecraft:bastion_remnant",
        "minecraft:the_nether -> minecraft:fortress",
        "minecraft:the_nether -> strange:nether_ruin",
        "minecraft:the_nether -> strange:citadel",
        "minecraft:the_end -> strange:stone_circle",
        "minecraft:the_end -> minecraft:endcity",
        "minecraft:the_end -> strange:end_ruin"
    ));

    @Config(name = "Biomes", description = "Biomes that runestones may teleport you to.  Format: [dimension -> structure]\nBiomes are listed in order of rarity and missing biomes will be skipped.")
    public static List<String> configBiomes = new ArrayList<>(Arrays.asList(
        "minecraft:overworld -> minecraft:flower_forest",
        "minecraft:overworld -> minecraft:ice_spikes",
        "minecraft:overworld -> minecraft:badlands"
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
        RunestoneLoot.create();
        RunestoneLocations.create();
    }

    private void handleServerStarted(MinecraftServer server) {
    }


}
