package svenhjol.strange.base;

import svenhjol.meson.handler.PacketHandler;
import svenhjol.strange.base.message.ClientUpdatePlayerState;
import svenhjol.strange.base.message.ServerUpdatePlayerState;
import svenhjol.strange.scrolls.message.*;
import svenhjol.strange.totems.message.ClientTotemUpdate;
import svenhjol.strange.totems.message.ClientTotemUpdateFlying;
import svenhjol.strange.traveljournal.message.ClientTravelJournalAction;
import svenhjol.strange.traveljournal.message.ClientTravelJournalEntries;
import svenhjol.strange.traveljournal.message.ServerTravelJournalAction;
import svenhjol.strange.traveljournal.message.ServerTravelJournalMeta;

public class StrangeMessages
{
    public static void init()
    {
        int index = 0;

        PacketHandler.HANDLER.registerMessage(index++, ServerQuestList.class, ServerQuestList::encode, ServerQuestList::decode, ServerQuestList.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ServerQuestAction.class, ServerQuestAction::encode, ServerQuestAction::decode, ServerQuestAction.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ServerScrollAction.class, ServerScrollAction::encode, ServerScrollAction::decode, ServerScrollAction.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ServerTravelJournalAction.class, ServerTravelJournalAction::encode, ServerTravelJournalAction::decode, ServerTravelJournalAction.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ServerTravelJournalMeta.class, ServerTravelJournalMeta::encode, ServerTravelJournalMeta::decode, ServerTravelJournalMeta.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ServerUpdatePlayerState.class, ServerUpdatePlayerState::encode, ServerUpdatePlayerState::decode, ServerUpdatePlayerState.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ClientQuestList.class, ClientQuestList::encode, ClientQuestList::decode, ClientQuestList.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ClientQuestAction.class, ClientQuestAction::encode, ClientQuestAction::decode, ClientQuestAction.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ClientScrollAction.class, ClientScrollAction::encode, ClientScrollAction::decode, ClientScrollAction.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ClientTotemUpdate.class, ClientTotemUpdate::encode, ClientTotemUpdate::decode, ClientTotemUpdate.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ClientTotemUpdateFlying.class, ClientTotemUpdateFlying::encode, ClientTotemUpdateFlying::decode, ClientTotemUpdateFlying.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ClientTravelJournalEntries.class, ClientTravelJournalEntries::encode, ClientTravelJournalEntries::decode, ClientTravelJournalEntries.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ClientTravelJournalAction.class, ClientTravelJournalAction::encode, ClientTravelJournalAction::decode, ClientTravelJournalAction.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ClientUpdatePlayerState.class, ClientUpdatePlayerState::encode, ClientUpdatePlayerState::decode, ClientUpdatePlayerState.Handler::handle);
    }
}
