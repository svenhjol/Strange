package svenhjol.strange.base;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.MesonLoader;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.strange.Strange;
import svenhjol.strange.runestones.message.ClientRunestonesInteract;
import svenhjol.strange.scrolls.message.ClientCurrentQuests;
import svenhjol.strange.scrolls.message.ServerCurrentQuests;
import svenhjol.strange.scrolls.message.ServerShowQuest;
import svenhjol.strange.totems.message.ClientTotemUpdate;
import svenhjol.strange.totems.message.ClientTotemUpdateFlying;
import svenhjol.strange.traveljournal.message.ClientTravelJournalAction;
import svenhjol.strange.traveljournal.message.ClientTravelJournalEntries;
import svenhjol.strange.traveljournal.message.ServerTravelJournalAction;
import svenhjol.strange.traveljournal.message.ServerTravelJournalMeta;

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
        PacketHandler.HANDLER.registerMessage(20, ServerCurrentQuests.class, ServerCurrentQuests::encode, ServerCurrentQuests::decode, ServerCurrentQuests.Handler::handle);
        PacketHandler.HANDLER.registerMessage(21, ServerShowQuest.class, ServerShowQuest::encode, ServerShowQuest::decode, ServerShowQuest.Handler::handle);
        PacketHandler.HANDLER.registerMessage(22, ServerTravelJournalAction.class, ServerTravelJournalAction::encode, ServerTravelJournalAction::decode, ServerTravelJournalAction.Handler::handle);
        PacketHandler.HANDLER.registerMessage(23, ServerTravelJournalMeta.class, ServerTravelJournalMeta::encode, ServerTravelJournalMeta::decode, ServerTravelJournalMeta.Handler::handle);
        PacketHandler.HANDLER.registerMessage(24, ClientCurrentQuests.class, ClientCurrentQuests::encode, ClientCurrentQuests::decode, ClientCurrentQuests.Handler::handle);
        PacketHandler.HANDLER.registerMessage(25, ClientTotemUpdate.class, ClientTotemUpdate::encode, ClientTotemUpdate::decode, ClientTotemUpdate.Handler::handle);
        PacketHandler.HANDLER.registerMessage(26, ClientTotemUpdateFlying.class, ClientTotemUpdateFlying::encode, ClientTotemUpdateFlying::decode, ClientTotemUpdateFlying.Handler::handle);
        PacketHandler.HANDLER.registerMessage(27, ClientTravelJournalEntries.class, ClientTravelJournalEntries::encode, ClientTravelJournalEntries::decode, ClientTravelJournalEntries.Handler::handle);
        PacketHandler.HANDLER.registerMessage(28, ClientTravelJournalAction.class, ClientTravelJournalAction::encode, ClientTravelJournalAction::decode, ClientTravelJournalAction.Handler::handle);
        PacketHandler.HANDLER.registerMessage(29, ClientRunestonesInteract.class, ClientRunestonesInteract::encode, ClientRunestonesInteract::decode, ClientRunestonesInteract.Handler::handle);

        // sounds
        RegistryHandler.registerSound(StrangeSounds.QUEST_ACTION_COMPLETE);
        RegistryHandler.registerSound(StrangeSounds.QUEST_ACTION_COUNT);
        RegistryHandler.registerSound(StrangeSounds.RUNESTONE_TRAVEL);
    }
}
