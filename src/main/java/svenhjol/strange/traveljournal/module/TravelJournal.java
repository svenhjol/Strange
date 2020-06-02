package svenhjol.strange.traveljournal.module;

import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.traveljournal.client.TravelJournalClient;
import svenhjol.strange.traveljournal.item.TravelJournalItem;
import svenhjol.strange.traveljournal.storage.TravelJournalSavedData;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TRAVEL_JOURNAL, hasSubscriptions = true,
    description = "Records interesting places around your world.")
public class TravelJournal extends MesonModule {
    public static TravelJournalItem item;

    @OnlyIn(Dist.CLIENT)
    public static TravelJournalClient client;

    @Config(name = "Maximum entries", description = "Maximum number of entries a single travel journal can hold.")
    public static int maxEntries = 30;

    @Config(name = "Always show X and Z co-ordinates", description = "Shows X and Z co-ordinates on the journal entry even when in survival mode.")
    public static boolean alwaysShowCoordinates = false;

    @Override
    public void init() {
        item = new TravelJournalItem(this);
    }

    @Override
    public void onClientSetup(FMLClientSetupEvent event) {
        client = new TravelJournalClient();
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld() instanceof ServerWorld) {
            TravelJournalSavedData.get((ServerWorld) event.getWorld());
        }
    }
}
