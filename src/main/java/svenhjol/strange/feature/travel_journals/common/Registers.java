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

        // Server-to-client packets
        registry.serverPacketSender(Networking.S2CTakePhoto.TYPE, Networking.S2CTakePhoto.CODEC);
        registry.serverPacketSender(Networking.S2CPhoto.TYPE, Networking.S2CPhoto.CODEC);

        // Client-to-server packets
        registry.clientPacketSender(Networking.C2SMakeBookmark.TYPE, Networking.C2SMakeBookmark.CODEC);
        registry.clientPacketSender(Networking.C2SPhoto.TYPE, Networking.C2SPhoto.CODEC);
        registry.clientPacketSender(Networking.C2SDownloadPhoto.TYPE, Networking.C2SDownloadPhoto.CODEC);
        registry.clientPacketSender(Networking.C2SUpdateBookmark.TYPE, Networking.C2SUpdateBookmark.CODEC);
        registry.clientPacketSender(Networking.C2SDeleteBookmark.TYPE, Networking.C2SDeleteBookmark.CODEC);
        registry.clientPacketSender(Networking.C2SExportBookmark.TYPE, Networking.C2SExportBookmark.CODEC);
        registry.clientPacketSender(Networking.C2SExportMap.TYPE, Networking.C2SExportMap.CODEC);

        // Server packet receivers
        registry.packetReceiver(Networking.C2SMakeBookmark.TYPE, () -> feature.handlers::makeBookmarkReceived);
        registry.packetReceiver(Networking.C2SPhoto.TYPE, () -> feature.handlers::photoReceived);
        registry.packetReceiver(Networking.C2SDownloadPhoto.TYPE, () -> feature.handlers::downloadPhotoReceived);
        registry.packetReceiver(Networking.C2SUpdateBookmark.TYPE, () -> feature.handlers::updateBookmarkReceived);
        registry.packetReceiver(Networking.C2SDeleteBookmark.TYPE, () -> feature.handlers::deleteBookmarkReceived);
        registry.packetReceiver(Networking.C2SExportBookmark.TYPE, () -> feature.handlers::exportBookmarkReceived);
        registry.packetReceiver(Networking.C2SExportMap.TYPE, () -> feature.handlers::exportMapReceived);
    }
}
