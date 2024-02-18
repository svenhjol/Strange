package svenhjol.strange.feature.quests.reward.function;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import svenhjol.strange.feature.quests.reward.RewardItem;
import svenhjol.strange.feature.quests.reward.RewardItemFunction;
import svenhjol.strange.feature.quests.reward.RewardItemFunctionParameters;

import javax.annotation.Nullable;
import java.util.List;

public class MakeTippedArrow implements RewardItemFunction {
    public static final String ID = "make_tipped_arrow";

    private static final List<String> DEFAULT_POTIONS = List.of(
        "night_vision", "invisibility", "leaping", "fire_resistance", "swiftness",
        "slowness", "water_breathing", "healing", "harming", "poison", "regeneration",
        "strength", "weakness", "luck", "slow_falling"
    );

    private Parameters params;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public RewardItemFunction withParameters(RewardItemFunctionParameters params) {
        this.params = new Parameters(params);
        return this;
    }

    @Override
    public void apply(RewardItem reward) {
        var quest = reward.quest;
        var stack = reward.stack;
        var random = quest.random();
        var potion = params.potion;

        if (stack.is(Items.TIPPED_ARROW)) {
            if (potion == null) {
                var def = DEFAULT_POTIONS.get(random.nextInt(DEFAULT_POTIONS.size()));
                potion = BuiltInRegistries.POTION.get(new ResourceLocation(def));
            }

            reward.stack = PotionUtils.setPotion(stack, potion);
        }
    }

    public static class Parameters {
        public @Nullable Potion potion;

        public Parameters(RewardItemFunctionParameters params) {
            this.potion = params.getPotion("potion", null);
        }
    }
}
