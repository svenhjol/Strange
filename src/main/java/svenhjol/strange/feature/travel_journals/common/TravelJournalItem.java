package svenhjol.strange.feature.travel_journals.common;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import svenhjol.charm.charmony.Resolve;
import svenhjol.charm.charmony.common.item.CharmItem;
import svenhjol.strange.feature.travel_journals.TravelJournals;
import svenhjol.strange.feature.travel_journals.TravelJournalsClient;

public class TravelJournalItem extends CharmItem<TravelJournals> {
    public TravelJournalItem() {
        super(new Properties());
    }

    @Override
    public Class<TravelJournals> typeForFeature() {
        return TravelJournals.class;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        var journal = JournalData.get(stack);
        var registers = feature().registers;
        
        // Check if holding a page in another hand.
        for (var interactionHand : InteractionHand.values()) {
            var held = player.getItemInHand(interactionHand);
            if (held.is(registers.travelJournalPageItem.get()) && BookmarkData.has(held)) {
                var bookmark = BookmarkData.get(held);
                if (!journal.isFull() && !journal.hasBookmark(bookmark.id())) {
                    // Add the page to the journal.
                    new JournalData.Mutable(journal)
                        .addBookmark(bookmark)
                        .save(stack);
                    
                    level.playSound(null, player.blockPosition(), registers.interactSound.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    held.shrink(1);
                    return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
                }
            }
        }
        
        if (level.isClientSide()) {
            Resolve.feature(TravelJournalsClient.class).handlers.openBookmarks(stack);
        }
        
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
