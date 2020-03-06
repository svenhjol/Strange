package svenhjol.strange.scrolls;

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
import svenhjol.strange.scrolls.capability.DummyCapability;
import svenhjol.strange.scrolls.capability.IQuestsCapability;
import svenhjol.strange.scrolls.capability.QuestsCapability;
import svenhjol.strange.scrolls.capability.QuestsStorage;
import svenhjol.strange.scrolls.client.QuestClient;
import svenhjol.strange.scrolls.event.QuestEvents;
import svenhjol.strange.scrolls.module.Scrolls;
import svenhjol.strange.scrolls.quest.Definition;
import svenhjol.strange.scrolls.quest.Generator;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import javax.annotation.Nullable;
import java.util.*;

public class Quests
{
    public static final String QUEST_ID = "questId";
    public static final int INTERVAL = 15;

    @CapabilityInject(IQuestsCapability.class)
    public static Capability<IQuestsCapability> QUESTS = null;
    public static Map<Integer, List<Definition>> available = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    public static QuestClient client;

    public Quests(Scrolls scrolls)
    { }

    public void onCommonSetup(FMLCommonSetupEvent event)
    {
        CapabilityManager.INSTANCE.register(IQuestsCapability.class, new QuestsStorage(), QuestsCapability::new); // register the interface, storage, and implementation
        MinecraftForge.EVENT_BUS.register(new QuestEvents()); // add all quest-related events to the bus
    }

    public void onServerStarted(FMLServerStartedEvent event)
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
                            hasMods = hasMods && Meson.isModuleEnabled(new ResourceLocation(s));
                        }
                        if (!hasMods) {
                            Meson.log("Quest " + res.toString() + " is missing required modules");
                            continue;
                        }
                    }

                    String name = res.getPath().replace("/", ".").replace(".json", "");
                    definition.setTitle(name);
                    definition.setTier(tier);
                    Quests.available.get(tier).add(definition);
                    Meson.debug("Loaded quest " + res.toString()+ " for tier " + tier);

                } catch (Exception e) {
                    Meson.warn("Could not load quest for " + res.toString() + " because " + e.getMessage());
                }
            }
        }
    }

    public void onClientSetup(FMLClientSetupEvent event)
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

    @Nullable
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
        return Math.max(1, Math.max(Scrolls.maxQuests, 3));
    }
}
