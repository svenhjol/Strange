package svenhjol.strange.scrolls.module;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
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
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.scrolls.capability.DummyCapability;
import svenhjol.strange.scrolls.capability.IQuestsCapability;
import svenhjol.strange.scrolls.capability.QuestsCapability;
import svenhjol.strange.scrolls.capability.QuestsStorage;
import svenhjol.strange.scrolls.client.QuestClient;
import svenhjol.strange.scrolls.event.QuestEvents;
import svenhjol.strange.scrolls.quest.Generator;
import svenhjol.strange.scrolls.quest.Generator.Definition;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.*;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SCROLLS, hasSubscriptions = true)
public class Quests extends MesonModule
{
    @Config(name = "Maximum quests", description = "Maximum number of quests a player can do at once.")
    public static int max = 3;

    @CapabilityInject(IQuestsCapability.class)
    public static Capability<IQuestsCapability> QUESTS = null;

    public static ResourceLocation QUESTS_CAP_ID = new ResourceLocation(Strange.MOD_ID, "quest_capability");

    public static Map<Integer, List<Definition>> available = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    public static QuestClient client;

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

        try {
            for (int tier = 1; tier <= 5; tier++) {
                Quests.available.put(tier, new ArrayList<>());
                Collection<ResourceLocation> resources = rm.getAllResourceLocations("quests/tier" + tier, file -> file.endsWith(".json"));

                for (ResourceLocation res : resources) {
                    IResource resource = rm.getResource(res);
                    Definition definition = Definition.deserialize(resource);
                    definition.tier = tier;
                    Quests.available.get(tier).add(definition);
                }
            }
        } catch (Exception e) {
            Meson.warn("Could not load quests", e);
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

    public static IQuest generate(World world, BlockPos pos, IQuest quest)
    {
        return Generator.INSTANCE.generate(world, pos, quest);
    }

    public static void update(PlayerEntity player)
    {
        Quests.getCapability(player).updateCurrentQuests(player);
    }

    public static void playActionCompleteSound(PlayerEntity player)
    {
        PlayerEntity p = player.world.isRemote ? player : null;
        player.world.playSound(p, player.getPosition(), StrangeSounds.QUEST_ACTION_COMPLETE, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    public static void playActionCountSound(PlayerEntity player)
    {
        PlayerEntity p = player.world.isRemote ? player : null;
        player.world.playSound(p, player.getPosition(), StrangeSounds.QUEST_ACTION_COUNT, SoundCategory.PLAYERS, 1.0F, ((player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.7F + 1.0F) * 1.1F);
    }
}
