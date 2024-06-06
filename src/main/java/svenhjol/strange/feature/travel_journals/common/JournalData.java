package svenhjol.strange.feature.travel_journals.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.charmony.Resolve;
import svenhjol.strange.feature.travel_journals.TravelJournals;

import java.util.ArrayList;
import java.util.List;

public record JournalData(List<BookmarkData> bookmarks) {
    private static final TravelJournals TRAVEL_JOURNALS = Resolve.feature(TravelJournals.class);
    private static final int MAX_BOOKMARKS = 8; // TODO: configurable!
    
    public static final Codec<JournalData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BookmarkData.CODEC.listOf().fieldOf("bookmarks")
            .forGetter(JournalData::bookmarks)
    ).apply(instance, JournalData::new));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, JournalData> STREAM_CODEC = StreamCodec.composite(
        BookmarkData.STREAM_CODEC.apply(ByteBufCodecs.list()),
            JournalData::bookmarks,
        JournalData::new
    );
    
    public static final JournalData EMPTY = new JournalData(List.of());
    
    public static Mutable create() {
        return new Mutable(EMPTY);
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
        return bookmarks.size() >= MAX_BOOKMARKS;
    }
    
    @SuppressWarnings("UnusedReturnValue")
    public static class Mutable {
        private final List<BookmarkData> bookmarks;
        
        public Mutable(JournalData data) {
            this.bookmarks = new ArrayList<>(data.bookmarks());
        }
        
        public Mutable addBookmark(BookmarkData bookmark) {
            this.bookmarks.add(bookmark);
            return this;
        }
        
        public JournalData save(ItemStack stack) {
            return JournalData.set(stack, this);
        }
        
        public JournalData toImmutable() {
            return new JournalData(bookmarks);
        }
    }
}
