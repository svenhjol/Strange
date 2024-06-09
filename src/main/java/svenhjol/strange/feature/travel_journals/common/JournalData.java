package svenhjol.strange.feature.travel_journals.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.charmony.Resolve;
import svenhjol.strange.feature.travel_journals.TravelJournals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record JournalData(UUID id, List<BookmarkData> bookmarks) {
    private static final TravelJournals TRAVEL_JOURNALS = Resolve.feature(TravelJournals.class);
    
    public static final Codec<JournalData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("id")
            .forGetter(JournalData::id),
        BookmarkData.CODEC.listOf().fieldOf("bookmarks")
            .forGetter(JournalData::bookmarks)
    ).apply(instance, JournalData::new));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, JournalData> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
            JournalData::id,
        BookmarkData.STREAM_CODEC.apply(ByteBufCodecs.list()),
            JournalData::bookmarks,
        JournalData::new
    );
    
    public static final JournalData EMPTY = new JournalData(UUID.randomUUID(), List.of());
    
    public static Mutable create() {
        return new Mutable(UUID.randomUUID(), EMPTY);
    }
    
    public static JournalData get(ItemStack stack) {
        return stack.getOrDefault(TRAVEL_JOURNALS.registers.journalData.get(), EMPTY);
    }
    
    public static JournalData set(ItemStack stack, Mutable mutable) {
        var immutable = mutable.toImmutable();
        stack.set(TRAVEL_JOURNALS.registers.journalData.get(), immutable);
        return immutable;
    }
    
    public static boolean has(ItemStack stack) {
        return stack.has(TRAVEL_JOURNALS.registers.journalData.get());
    }
    
    public boolean isFull() {
        return bookmarks.size() >= TRAVEL_JOURNALS.numberOfBookmarksPerJournal();
    }
    
    public Optional<BookmarkData> getBookmark(UUID bookmarkId) {
        return bookmarks.stream().filter(b -> b.id().equals(bookmarkId)).findFirst();
    }
    
    public boolean hasBookmark(UUID bookmarkId) {
        return getBookmark(bookmarkId).isPresent();
    }
    
    @SuppressWarnings("UnusedReturnValue")
    public static class Mutable {
        private final UUID id;
        private final List<BookmarkData> bookmarks;
        
        public Mutable(JournalData data) {
            this(data.id(), data);
        }
        
        public Mutable(UUID id, JournalData data) {
            this.id = id;
            this.bookmarks = new ArrayList<>(data.bookmarks());
        }
        
        public Optional<BookmarkData> getBookmark(UUID bookmarkId) {
            return bookmarks.stream().filter(b -> b.id().equals(bookmarkId)).findFirst();
        }
        
        public Mutable addBookmark(BookmarkData bookmark) {
            bookmarks.add(bookmark);
            return this;
        }
        
        public Mutable updateBookmark(BookmarkData bookmark) {
            var existing = getBookmark(bookmark.id()).orElseThrow();
            bookmarks.set(bookmarks.indexOf(existing), bookmark);
            return this;
        }
        
        public Mutable deleteBookmark(UUID bookmarkId) {
            var existing = getBookmark(bookmarkId).orElseThrow();
            bookmarks.remove(existing);
            return this;
        }
        
        public JournalData save(ItemStack stack) {
            return JournalData.set(stack, this);
        }
        
        public JournalData toImmutable() {
            return new JournalData(id, bookmarks);
        }
    }
}
