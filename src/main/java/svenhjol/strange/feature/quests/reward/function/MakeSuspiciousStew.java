package svenhjol.strange.feature.quests.reward.function;

import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.minecraft.world.level.block.SuspiciousEffectHolder.EffectEntry;
import svenhjol.strange.feature.quests.reward.RewardItem;
import svenhjol.strange.feature.quests.reward.RewardItemFunction;
import svenhjol.strange.feature.quests.reward.RewardItemFunctionParameters;

import java.util.ArrayList;
import java.util.List;

public class MakeSuspiciousStew implements RewardItemFunction {
    public static final String ID = "make_suspicious_stew";

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
        var effects = new ArrayList<>(params.effects);
        EffectEntry effect;

        if (stack.is(Items.SUSPICIOUS_STEW)) {
            if (effects.isEmpty()) {
                List<EffectEntry> holders = new ArrayList<>();
                for (var holder : SuspiciousEffectHolder.getAllEffectHolders()) {
                    holders.addAll(holder.getSuspiciousEffects());
                }
                if (holders.isEmpty()) {
                    throw new RuntimeException("Could not get default effect holders");
                }

                // If no effects specified, take a random one from the list of all effects
                Util.shuffle(holders, random);
                var first = holders.get(0);
                effect = new EffectEntry(first.effect(), params.duration);
            } else {
                Util.shuffle(effects, random);
                effect = effects.get(0);
            }

            SuspiciousStewItem.appendMobEffects(stack, List.of(effect));
            reward.stack = stack;
        }
    }

    public static class Parameters {
        public List<EffectEntry> effects = new ArrayList<>();
        public int duration;

        public Parameters(RewardItemFunctionParameters params) {
            this.duration = params.getInteger("duration", 5) * 20;
            var effects = params.getResourceLocationList("effects", List.of());

            for (var effect : effects) {
                var mobEffect = BuiltInRegistries.MOB_EFFECT.get(effect);
                if (mobEffect == null) continue;
                this.effects.add(new EffectEntry(mobEffect, duration));
            }
        }
    }
}
