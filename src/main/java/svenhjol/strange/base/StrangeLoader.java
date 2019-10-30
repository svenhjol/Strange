package svenhjol.strange.base;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.MesonLoader;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.strange.Strange;
import svenhjol.strange.base.message.*;
import svenhjol.strange.runestones.message.ClientInteractMessage;
import svenhjol.strange.traveljournal.message.ServerActionMessage;
import svenhjol.strange.traveljournal.message.ClientActionMessage;
import svenhjol.strange.traveljournal.message.ClientEntriesMessage;
import svenhjol.strange.traveljournal.message.ServerMetaMessage;

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
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, ClientInteractMessage.class, ClientInteractMessage::encode, ClientInteractMessage::decode, ClientInteractMessage.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, RequestShowQuest.class, RequestShowQuest::encode, RequestShowQuest::decode, RequestShowQuest.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, UpdateTotemMessage.class, UpdateTotemMessage::encode, UpdateTotemMessage::decode, UpdateTotemMessage.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, UpdateFlying.class, UpdateFlying::encode, UpdateFlying::decode, UpdateFlying.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, ServerActionMessage.class, ServerActionMessage::encode, ServerActionMessage::decode, ServerActionMessage.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, ServerMetaMessage.class, ServerMetaMessage::encode, ServerMetaMessage::decode, ServerMetaMessage.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, ClientEntriesMessage.class, ClientEntriesMessage::encode, ClientEntriesMessage::decode, ClientEntriesMessage.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, ClientActionMessage.class, ClientActionMessage::encode, ClientActionMessage::decode, ClientActionMessage.Handler::handle);

        // sounds
        RegistryHandler.registerSound(StrangeSounds.QUEST_ACTION_COMPLETE);
        RegistryHandler.registerSound(StrangeSounds.QUEST_ACTION_COUNT);
        RegistryHandler.registerSound(StrangeSounds.RUNESTONE_TRAVEL);
    }
}
