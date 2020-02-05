package svenhjol.strange.scrolls.module;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonLoader;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.scrolls.capability.DummyCapability;
import svenhjol.strange.scrolls.capability.IQuestsCapability;
import svenhjol.strange.scrolls.capability.QuestsCapability;
import svenhjol.strange.scrolls.capability.QuestsStorage;
import svenhjol.strange.scrolls.client.QuestClient;
import svenhjol.strange.scrolls.event.QuestEvents;
import svenhjol.strange.scrolls.quest.Definition;
import svenhjol.strange.scrolls.quest.Generator;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.*;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SCROLLS, hasSubscriptions = true, configureEnabled = false)
public class Quests extends MesonModule
{
    public static final ResourceLocation QUESTS_CAP_ID = new ResourceLocation(Strange.MOD_ID, "quest_capability");
    public static final String QUEST_ID = "questId";

    @Config(name = "Maximum quests", description = "Maximum number of quests a player can do at once (Maximum 3)")
    public static int maxQuests = 2; // don't get this value directly, use getMaxQuests()

    @Config(name = "Encounter distance", description = "Distance from quest start (in blocks) that a mob will spawn for 'encounter' quests.")
    public static int encounterDistance = 600;

    @Config(name = "Locate distance", description = "Distance from quest start (in blocks) that a treasure chest will spawn for 'locate' quests.")
    public static int locateDistance = 400;

    @Config(name = "Language", description = "Language code to use for showing quest details.")
    public static String language = "en";

    @Config(name = "Tick check interval", description = "Interval between ticks that quest updates and checks occur.\nSet higher to decrease game loop processing at the expense of quest action lag.")
    public static int tickInterval = 15;

    @CapabilityInject(IQuestsCapability.class)
    public static Capability<IQuestsCapability> QUESTS = null;
    public static Map<Integer, List<Definition>> available = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    public static QuestClient client;

    @Override
    public boolean isEnabled()
    {
        return super.isEnabled() && Strange.hasModule(Scrolls.class);
    }

    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        CapabilityManager.INSTANCE.register(IQuestsCapability.class, new QuestsStorage(), QuestsCapability::new); // register the interface, storage, and implementation
        MinecraftForge.EVENT_BUS.register(new QuestEvents()); // add all quest-related events to the bus
    }

    @Override
    public void serverStarted(FMLServerStartedEvent event)
    {
        IReloadableResourceManager rm = event.getServer().getResourceManager();

        for (int tier = 1; tier <= Scrolls.MAX_TIERS; tier++) {
            Quests.available.put(tier, new ArrayList<>());
            Collection<ResourceLocation> resources = rm.getAllResourceLocations("quests/tier" + tier, file -> file.endsWith(".json"));

            for (ResourceLocation res : resources) {
                try {
                    IResource resource = rm.getResource(res);
                    Definition definition = Definition.deserialize(resource);

                    List<String> mods = definition.getModules();
                    if (!mods.isEmpty()) {
                        boolean hasMods = true;
                        for (String s : mods) {
                            hasMods = hasMods && MesonLoader.hasModule(new ResourceLocation(s));
                        }
                        if (!hasMods) {
                            Meson.log("Quest " + definition.getTitle() + " is missing required modules");
                            continue;
                        }
                    }

                    String name = res.getPath().replace("/", ".").replace(".json", "");
                    definition.setTitle(name);
                    definition.setTier(tier);
                    Quests.available.get(tier).add(definition);
                    Meson.debug(this, "Loaded quest " + definition.getTitle() + " for tier " + tier);

                } catch (Exception e) {
                    Meson.warn(this, "Could not load quest for " + res + " because " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void setupClient(FMLClientSetupEvent event)
    {
        client = new QuestClient();
        MinecraftForge.EVENT_BUS.register(client);
    }

    public static IQuestsCapability getCapability(PlayerEntity player)
    {
        return player.getCapability(QUESTS, null).orElse(new DummyCapability());
    }

    public static List<IQuest> getCurrent(PlayerEntity player)
    {
        return getCapability(player).getCurrentQuests(player);
    }

    public static Optional<IQuest> getCurrentQuestById(PlayerEntity player, String id)
    {
        return Quests.getCurrent(player).stream()
            .filter(q -> q.getId().equals(id))
            .findFirst();
    }

    public static IQuest generate(World world, BlockPos pos, float valueMultiplier, IQuest quest)
    {
        return Generator.INSTANCE.generate(world, pos, valueMultiplier, quest);
    }

    public static void update(PlayerEntity player)
    {
        Quests.getCapability(player).updateCurrentQuests(player);
    }

    public static int getMaxQuests()
    {
        return Math.min(1, Math.max(maxQuests, 3));
    }
}
