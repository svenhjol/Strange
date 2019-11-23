package svenhjol.strange.base;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.MesonLoader;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.strange.Strange;
import svenhjol.strange.magic.message.ServerStaffAction;
import svenhjol.strange.runestones.message.ClientRunestonesInteract;
import svenhjol.strange.scrolls.message.*;
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

        int index = 20;

        // messages
        PacketHandler.HANDLER.registerMessage(index++, ServerQuestList.class, ServerQuestList::encode, ServerQuestList::decode, ServerQuestList.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ServerQuestAction.class, ServerQuestAction::encode, ServerQuestAction::decode, ServerQuestAction.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ServerScrollAction.class, ServerScrollAction::encode, ServerScrollAction::decode, ServerScrollAction.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ServerTravelJournalAction.class, ServerTravelJournalAction::encode, ServerTravelJournalAction::decode, ServerTravelJournalAction.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ServerTravelJournalMeta.class, ServerTravelJournalMeta::encode, ServerTravelJournalMeta::decode, ServerTravelJournalMeta.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ClientQuestList.class, ClientQuestList::encode, ClientQuestList::decode, ClientQuestList.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ClientQuestAction.class, ClientQuestAction::encode, ClientQuestAction::decode, ClientQuestAction.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ClientScrollAction.class, ClientScrollAction::encode, ClientScrollAction::decode, ClientScrollAction.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ClientTotemUpdate.class, ClientTotemUpdate::encode, ClientTotemUpdate::decode, ClientTotemUpdate.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ClientTotemUpdateFlying.class, ClientTotemUpdateFlying::encode, ClientTotemUpdateFlying::decode, ClientTotemUpdateFlying.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ClientTravelJournalEntries.class, ClientTravelJournalEntries::encode, ClientTravelJournalEntries::decode, ClientTravelJournalEntries.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ClientTravelJournalAction.class, ClientTravelJournalAction::encode, ClientTravelJournalAction::decode, ClientTravelJournalAction.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ClientRunestonesInteract.class, ClientRunestonesInteract::encode, ClientRunestonesInteract::decode, ClientRunestonesInteract.Handler::handle);
        PacketHandler.HANDLER.registerMessage(index++, ServerStaffAction.class, ServerStaffAction::encode, ServerStaffAction::decode, ServerStaffAction.Handler::handle);

        // sounds
        StrangeSounds.soundsToRegister.forEach(RegistryHandler::registerSound);
    }

    // TODO move this to WorldHelper
    public static BlockRayTraceResult getBlockLookedAt(PlayerEntity player)
    {
        int len = 6;
        Vec3d vec3d = player.getEyePosition(1.0F);
        Vec3d vec3d1 = player.getLook(1.0F);
        return player.world.rayTraceBlocks(new RayTraceContext(vec3d, vec3d.add(vec3d1.x * len, vec3d1.y * len, vec3d1.z * len), RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));
    }
}
