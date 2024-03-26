package svenhjol.strange.feature.quests.reward.function;

import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import svenhjol.strange.feature.quests.reward.RewardItem;
import svenhjol.strange.feature.quests.reward.RewardItemFunction;
import svenhjol.strange.feature.quests.reward.RewardItemFunctionParameters;

import java.util.ArrayList;
import java.util.List;

public class MakePotion implements RewardItemFunction {
    public static final String ID = "make_potion";

    private static final List<String> DEFAULT_POTIONS = List.of(
        "night_vision", "invisibility", "leaping", "fire_resistance", "swiftness",
        "water_breathing", "healing", "harming", "poison", "regeneration",
        "strength", "luck", "slow_falling"
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
        var potions = new ArrayList<>(params.potions);

        if (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) {
            if (potions.isEmpty()) {
                var def = DEFAULT_POTIONS.get(random.nextInt(DEFAULT_POTIONS.size()));
                potions.add(BuiltInRegistries.POTION.get(new ResourceLocation(def)));
            }

            Util.shuffle(potions, random);
            var potion = potions.get(0);
            reward.stack = PotionUtils.setPotion(stack, potion);
        }
    }

    public static class Parameters {
        public List<Potion> potions = new ArrayList<>();
        public int duration;

        public Parameters(RewardItemFunctionParameters params) {
            var effects = params.getResourceLocationList("potions", List.of());

            for (var effect : effects) {
                var potion = BuiltInRegistries.POTION.get(effect);
                this.potions.add(potion);
            }
        }
    }
}
