package svenhjol.strange.base;

import svenhjol.meson.MesonInstance;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.strange.scrolls.message.*;
import svenhjol.strange.totems.message.ClientTotemUpdate;
import svenhjol.strange.totems.message.ClientTotemUpdateFlying;
import svenhjol.strange.traveljournal.message.ClientTravelJournalAction;
import svenhjol.strange.traveljournal.message.ClientTravelJournalEntries;
import svenhjol.strange.traveljournal.message.ServerTravelJournalAction;
import svenhjol.strange.traveljournal.message.ServerTravelJournalMeta;

public class StrangeMessages {
    public static void init(MesonInstance instance) {
        PacketHandler handler = instance.getPacketHandler();

        handler.register(ServerQuestList.class, ServerQuestList::encode, ServerQuestList::decode, ServerQuestList.Handler::handle);
        handler.register(ServerQuestAction.class, ServerQuestAction::encode, ServerQuestAction::decode, ServerQuestAction.Handler::handle);
        handler.register(ServerScrollAction.class, ServerScrollAction::encode, ServerScrollAction::decode, ServerScrollAction.Handler::handle);
        handler.register(ServerTravelJournalAction.class, ServerTravelJournalAction::encode, ServerTravelJournalAction::decode, ServerTravelJournalAction.Handler::handle);
        handler.register(ServerTravelJournalMeta.class, ServerTravelJournalMeta::encode, ServerTravelJournalMeta::decode, ServerTravelJournalMeta.Handler::handle);
        handler.register(ClientQuestList.class, ClientQuestList::encode, ClientQuestList::decode, ClientQuestList.Handler::handle);
        handler.register(ClientQuestAction.class, ClientQuestAction::encode, ClientQuestAction::decode, ClientQuestAction.Handler::handle);
        handler.register(ClientScrollAction.class, ClientScrollAction::encode, ClientScrollAction::decode, ClientScrollAction.Handler::handle);
        handler.register(ClientTotemUpdate.class, ClientTotemUpdate::encode, ClientTotemUpdate::decode, ClientTotemUpdate.Handler::handle);
        handler.register(ClientTotemUpdateFlying.class, ClientTotemUpdateFlying::encode, ClientTotemUpdateFlying::decode, ClientTotemUpdateFlying.Handler::handle);
        handler.register(ClientTravelJournalEntries.class, ClientTravelJournalEntries::encode, ClientTravelJournalEntries::decode, ClientTravelJournalEntries.Handler::handle);
        handler.register(ClientTravelJournalAction.class, ClientTravelJournalAction::encode, ClientTravelJournalAction::decode, ClientTravelJournalAction.Handler::handle);
    }
}
