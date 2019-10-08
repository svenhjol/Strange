package svenhjol.strange.base;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.MesonLoader;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.strange.Strange;
import svenhjol.strange.base.message.*;
import svenhjol.strange.traveljournal.message.ActionMessage;
import svenhjol.strange.traveljournal.message.ClientActionMessage;
import svenhjol.strange.traveljournal.message.ClientEntriesMessage;
import svenhjol.strange.traveljournal.message.MetaMessage;

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

        // messages
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, RequestCurrentQuests.class, RequestCurrentQuests::encode, RequestCurrentQuests::decode, RequestCurrentQuests.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, SendCurrentQuests.class, SendCurrentQuests::encode, SendCurrentQuests::decode, SendCurrentQuests.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, RunestoneActivated.class, RunestoneActivated::encode, RunestoneActivated::decode, RunestoneActivated.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, RequestShowQuest.class, RequestShowQuest::encode, RequestShowQuest::decode, RequestShowQuest.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, UpdateTotemMessage.class, UpdateTotemMessage::encode, UpdateTotemMessage::decode, UpdateTotemMessage.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, UpdateFlying.class, UpdateFlying::encode, UpdateFlying::decode, UpdateFlying.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, ActionMessage.class, ActionMessage::encode, ActionMessage::decode, ActionMessage.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, MetaMessage.class, MetaMessage::encode, MetaMessage::decode, MetaMessage.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, ClientEntriesMessage.class, ClientEntriesMessage::encode, ClientEntriesMessage::decode, ClientEntriesMessage.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, ClientActionMessage.class, ClientActionMessage::encode, ClientActionMessage::decode, ClientActionMessage.Handler::handle);

        // sounds
        RegistryHandler.registerSound(StrangeSounds.QUEST_ACTION_COMPLETE);
        RegistryHandler.registerSound(StrangeSounds.QUEST_ACTION_COUNT);
        RegistryHandler.registerSound(StrangeSounds.RUNESTONE_TRAVEL);
    }
}
