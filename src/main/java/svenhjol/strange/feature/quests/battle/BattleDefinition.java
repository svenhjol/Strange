package svenhjol.strange.feature.quests.battle;

import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import svenhjol.strange.Strange;
import svenhjol.strange.data.LinkedEntityTypeList;
import svenhjol.strange.data.LinkedResourceList;
import svenhjol.strange.feature.quests.QuestDefinition;
import svenhjol.strange.feature.quests.Quests;
import svenhjol.strange.feature.quests.BaseDefinition;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BattleDefinition extends BaseDefinition<BattleDefinition> {
    static final ResourceLocation DEFAULT_BATTLE_EFFECTS = new ResourceLocation(Strange.ID, "default_battle_effects");
    private ResourceLocation list;
    private int amount = 1;
    private int weight = -1;
    private final List<ResourceLocation> effects = new ArrayList<>();

    public BattleDefinition(QuestDefinition definition) {
        super(definition);
        effects.add(DEFAULT_BATTLE_EFFECTS);
    }

    @Override
    public BattleDefinition fromMap(Map<String, Object> map) {
        for (var entry : map.entrySet()) {
            var key = entry.getKey();
            var val = entry.getValue();

            switch (key) {
                case "list": {
                    this.list = parseResourceLocation(val).orElseThrow();
                    break;
                }
                case "amount": {
                    this.amount = parseInteger(val).orElseThrow();
                    break;
                }
                case "weight": {
                    this.weight = parseInteger(val).orElseThrow();
                    break;
                }
                case "effects": {
                    this.effects.addAll(parseResourceLocationList(val).orElseThrow());
                    break;
                }
            }
        }

        return this;
    }

    @Override
    protected String dataDir() {
        return "quests/battle";
    }

    public List<EntityType<?>> mobs() {
        List<EntityType<?>> out = new ArrayList<>();

        var mobs = LinkedEntityTypeList.load(entries().getOrDefault(list, new LinkedList<>()));
        if (mobs.isEmpty()) {
            throw new RuntimeException("Battle entity list is empty: " + list);
        }

        Util.shuffle(mobs, random());

        var max = Math.min(Quests.maxQuestRequirements, amount);
        for (int i = 0; i < max; i++) {
            EntityType<?> mob;
            if (i < mobs.size()) {
                mob = mobs.get(i);
            } else {
                mob = mobs.get(random().nextInt(mobs.size()));
            }
            out.add(mob);
        }

        return out;
    }

    public List<MobEffectInstance> effects() {
        List<MobEffectInstance> out = new ArrayList<>();

        for (var effectId : effects) {
            var keys = LinkedResourceList.load(entries().getOrDefault(effectId, new LinkedList<>()));
            for (var key : keys) {
                var effect = BuiltInRegistries.MOB_EFFECT.get(key);
                if (effect == null) continue;

                // Check that this effect hasn't already been added.
                if (out.stream().anyMatch(e -> e.getEffect().equals(effect))) {
                    continue;
                }

                var amplifier = Math.max(0, Math.min(3, definition.level() - 2));
                var duration = effect.isInstantenous() ? 1 : 100000;
                out.add(new MobEffectInstance(effect, duration, amplifier));
            }
        }

        return out;
    }
}
