package svenhjol.strange.scrolls.module;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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
import svenhjol.strange.scrolls.client.screen.QuestScreen;
import svenhjol.strange.scrolls.client.toast.QuestToast;
import svenhjol.strange.scrolls.event.QuestEvents;
import svenhjol.strange.scrolls.quest.Generator;
import svenhjol.strange.scrolls.quest.Generator.Definition;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SCROLLS, hasSubscriptions = true)
public class Quests extends MesonModule
{
    @Config(name = "Maximum quests", description = "Maximum number of quests a player can do at once.")
    public static int max = 3;

    @CapabilityInject(IQuestsCapability.class)
    public static Capability<IQuestsCapability> QUESTS = null;

    public static ResourceLocation QUESTS_CAP_ID = new ResourceLocation(Strange.MOD_ID, "quest_capability");

    public static Map<Integer, List<Definition>> available = new HashMap<>();

    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        CapabilityManager.INSTANCE.register(IQuestsCapability.class, new QuestsStorage(), QuestsCapability::new); // register the interface, storage, and implementation
        MinecraftForge.EVENT_BUS.register(new QuestEvents()); // add all quest-related events to the bus
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

    public static IQuest generate(IQuest quest, World world)
    {
        int tier = quest.getTier();
        if (tier < 1) {
            throw new RuntimeException("Quest is missing a tier");
        }

        return Generator.INSTANCE.generate(world, tier);
    }

    public static void update(PlayerEntity player)
    {
        Quests.getCapability(player).updateCurrentQuests(player);
    }

    public static void showQuestScreen(PlayerEntity player, IQuest quest)
    {
        Minecraft.getInstance().displayGuiScreen(new QuestScreen(player, quest));
    }

    public static void showQuestScreen(PlayerEntity player, ItemStack stack)
    {
        Minecraft.getInstance().displayGuiScreen(new QuestScreen(player, stack));
    }

    public static void playActionCompleteSound(PlayerEntity player)
    {
        player.world.playSound(null, player.getPosition(), StrangeSounds.QUEST_ACTION_COMPLETE, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    public static void playActionCountSound(PlayerEntity player)
    {
        player.world.playSound(null, player.getPosition(), StrangeSounds.QUEST_ACTION_COUNT, SoundCategory.PLAYERS, 1.0F, ((player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.7F + 1.0F) * 1.1F);
    }

    public static void toast(IQuest quest, QuestToast.Type type, String title)
    {
        Minecraft.getInstance().getToastGui().add(new QuestToast(quest, type, title, quest.getTitle()));
    }
}
