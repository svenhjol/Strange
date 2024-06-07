package svenhjol.strange.feature.travel_journals.common;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import svenhjol.charm.charmony.feature.RegisterHolder;
import svenhjol.strange.feature.travel_journals.TravelJournals;

import java.util.function.Supplier;

public final class Registers extends RegisterHolder<TravelJournals> {
    public final Supplier<Item> item;
    public final Supplier<DataComponentType<JournalData>> journalData;
    public final Supplier<DataComponentType<BookmarkData>> bookmarkData;
    public final Supplier<DataComponentType<BookmarkExtraData>> bookmarkExtraData;
    public final Supplier<SoundEvent> interactSound;
    public final Supplier<SoundEvent> photoSound;

    public Registers(TravelJournals feature) {
        super(feature);
        var registry = feature.registry();

        item = registry.item("travel_journal", TravelJournalItem::new);
        
        // Data components
        journalData = registry.dataComponent("travel_journal",
            () -> builder -> builder
                .persistent(JournalData.CODEC)
                .networkSynchronized(JournalData.STREAM_CODEC));
        
        bookmarkData = registry.dataComponent("bookmark",
            () -> builder -> builder
                .persistent(BookmarkData.CODEC)
                .networkSynchronized(BookmarkData.STREAM_CODEC));
        
        bookmarkExtraData = registry.dataComponent("bookmark_extra",
            () -> builder -> builder
                .persistent(BookmarkExtraData.CODEC)
                .networkSynchronized(BookmarkExtraData.STREAM_CODEC));

        // Sounds
        interactSound = registry.soundEvent("travel_journal_interact");
        photoSound = registry.soundEvent("travel_journal_photo");

        // Client-to-server packets
        registry.clientPacketSender(Networking.C2SMakeBookmark.TYPE, Networking.C2SMakeBookmark.CODEC);
        registry.clientPacketSender(Networking.C2SSendPhoto.TYPE, Networking.C2SSendPhoto.CODEC);

        // Server-to-client packets
        registry.serverPacketSender(Networking.S2CTakePhoto.TYPE, Networking.S2CTakePhoto.CODEC);

        // Server packet receivers
        registry.packetReceiver(Networking.C2SMakeBookmark.TYPE, () -> feature.handlers::makeBookmarkReceived);
        registry.packetReceiver(Networking.C2SSendPhoto.TYPE, () -> feature.handlers::sendPhotoReceived);
    }
}
