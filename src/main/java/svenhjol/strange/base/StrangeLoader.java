package svenhjol.strange.base;

import net.minecraft.world.gen.feature.jigsaw.IJigsawDeserializer;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonLoader;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.helper.ForgeHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.base.message.ClientUpdatePlayerState;
import svenhjol.strange.base.message.ServerUpdatePlayerState;
import svenhjol.strange.base.compat.QuarkBigDungeons;
import svenhjol.strange.base.compat.QuarkCaveRoots;
import svenhjol.strange.base.compat.QuarkCompat;
import svenhjol.strange.base.compat.QuarkVariantChests;
import svenhjol.strange.base.feature.StrangeJigsawPiece;
import svenhjol.strange.scrolls.message.*;
import svenhjol.strange.totems.message.ClientTotemUpdate;
import svenhjol.strange.totems.message.ClientTotemUpdateFlying;
import svenhjol.strange.traveljournal.message.ClientTravelJournalAction;
import svenhjol.strange.traveljournal.message.ClientTravelJournalEntries;
import svenhjol.strange.traveljournal.message.ServerTravelJournalAction;
import svenhjol.strange.traveljournal.message.ServerTravelJournalMeta;

public class StrangeLoader extends MesonLoader
{
    public static QuarkCompat quarkCompat;
    public static QuarkBigDungeons quarkBigDungeons;
    public static QuarkCaveRoots quarkCaveRoots;
    public static QuarkVariantChests quarkVariantChests;
    public static StrangeClient client;

    public StrangeLoader()
    {
        super(Strange.MOD_ID);
    }

    @Override
    public void earlyInit()
    {
        // sounds
        StrangeSounds.init();

        // loot
        StrangeLoot.init();

        // compat
        try {
            if (ForgeHelper.isModLoaded("quark")) {
                quarkCompat = QuarkCompat.class.newInstance();
                quarkBigDungeons = QuarkBigDungeons.class.newInstance();
                quarkCaveRoots = QuarkCaveRoots.class.newInstance();
                quarkVariantChests = QuarkVariantChests.class.newInstance();
            }
        } catch (Exception e) {
            Meson.warn("Error loading Quark compat class", e);
        }

        StrangeJigsawPiece.STRANGE_POOL_ELEMENT = IJigsawDeserializer.register("strange_pool_element", StrangeJigsawPiece::new);
    }

    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        int index = 20;

        // messages
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

        super.setup(event);
    }

    @Override
    public void setupClient(FMLClientSetupEvent event)
    {
        super.setupClient(event);
        client = new StrangeClient();
    }
}
