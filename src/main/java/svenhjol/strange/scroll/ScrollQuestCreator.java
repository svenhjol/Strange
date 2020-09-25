package svenhjol.strange.scroll;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.strange.helper.ScrollHelper;
import svenhjol.strange.scroll.populator.Populator;
import svenhjol.strange.scroll.populator.RewardPopulator;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ScrollQuestCreator {
    public static ScrollQuest create(PlayerEntity player, ScrollDefinition definition, int rarity) {
        return create(player, definition, rarity, null);
    }

    public static ScrollQuest create(PlayerEntity player, ScrollDefinition definition, int rarity, @Nullable MerchantEntity merchant) {
        UUID merchantUid = merchant != null ? merchant.getUuid() : ScrollHelper.ANY_MERCHANT;
        World world = player.world;
        BlockPos pos = player.getBlockPos();
        ScrollQuest quest = new ScrollQuest(merchantUid, rarity);

        quest.setTitle(definition.getTitle());
        quest.setMerchant(merchantUid);

        List<Populator> populators = new ArrayList<>(Arrays.asList(
            new RewardPopulator(world, pos, quest, definition)
        ));

        populators.forEach(Populator::populate);
        return quest;
    }
}
