package svenhjol.strange.feature.travel_journals.common;

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
        var held = player.getItemInHand(hand);
        
        if (level.isClientSide()) {
            Resolve.feature(TravelJournalsClient.class).handlers.openBookmarks(held, 1);
        }
        
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, held);
    }
}
