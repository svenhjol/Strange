package svenhjol.strange.base;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.MesonLoader;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.strange.Strange;
import svenhjol.strange.base.message.RequestCurrentQuests;
import svenhjol.strange.base.message.RequestShowQuest;
import svenhjol.strange.base.message.RunestoneActivated;
import svenhjol.strange.base.message.SendCurrentQuests;

public class StrangeLoader extends MesonLoader
{
    public StrangeLoader()
    {
        super(Strange.MOD_ID);
    }

    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        super.setup(event);

        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, RequestCurrentQuests.class, RequestCurrentQuests::encode, RequestCurrentQuests::decode, RequestCurrentQuests.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, SendCurrentQuests.class, SendCurrentQuests::encode, SendCurrentQuests::decode, SendCurrentQuests.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, RunestoneActivated.class, RunestoneActivated::encode, RunestoneActivated::decode, RunestoneActivated.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, RequestShowQuest.class, RequestShowQuest::encode, RequestShowQuest::decode, RequestShowQuest.Handler::handle);

        RegistryHandler.registerSound(StrangeSounds.QUEST_ACTION_COMPLETE);
        RegistryHandler.registerSound(StrangeSounds.QUEST_ACTION_COUNT);
        RegistryHandler.registerSound(StrangeSounds.RUNESTONE_TRAVEL);
    }
}
