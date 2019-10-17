package svenhjol.strange.traveljournal.module;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.traveljournal.item.TravelJournalItem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TRAVEL_JOURNAL, hasSubscriptions = true)
public class TravelJournal extends MesonModule
{
    public static final int QUEUE_FLUSH_TIME = 10;

    public static TravelJournalItem item;
    public static Map<Long, Map<PlayerEntity, Consumer<PlayerEntity>>> queue = new HashMap<>();

    @Config(name = "Maximum entries", description = "Maximum number of entries a single travel journal can hold.")
    public static int maxEntries = 15;

    @Override
    public void init()
    {
        item = new TravelJournalItem(this);
    }

    @SubscribeEvent
    public void flushQueue(PlayerTickEvent event)
    {
        if (!event.player.world.isRemote) {
            long time = event.player.world.getGameTime();

            Iterator<Long> i = queue.keySet().iterator();

            while (i.hasNext()) {
                Long l = i.next();
                if (time - l >= QUEUE_FLUSH_TIME) {
                    for (Map.Entry<PlayerEntity, Consumer<PlayerEntity>> entry : queue.get(l).entrySet()) {
                        PlayerEntity player = entry.getKey();
                        Consumer<PlayerEntity> run = entry.getValue();
                        run.accept(player);
                        Meson.debug("Queue: flushed", run);
                    }
                    i.remove();
                }
            }
        }
    }

    public static void addToQueue(long time, PlayerEntity player, Consumer<PlayerEntity> event)
    {
        if (!queue.containsKey(time)) {
            queue.put(time, new HashMap<>());
        }
        queue.get(time).put(player, event);
        Meson.debug("Queue: added", event);
    }
}
