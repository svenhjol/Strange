package svenhjol.strange.feature.quests.reward;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.alchemy.Potion;
import svenhjol.strange.data.SimpleObjectParser;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardItemFunctionParameters implements SimpleObjectParser {
    private final RewardItemFunctionDefinition functionDefinition;
    private final Map<String, Object> map = new HashMap<>();
    public RewardItemFunctionParameters(RewardItemFunctionDefinition functionDefinition) {
        this.functionDefinition = functionDefinition;
    }

    public RewardItemFunctionDefinition functionDefinition() {
        return this.functionDefinition;
    }

    public void add(String key, Object val) {
        map.put(key, val);
    }

    @Override
    public RandomSource random() {
        return functionDefinition.random();
    }

    public boolean getBoolean(String key, boolean fallback) {
        return parseBoolean(map.get(key)).orElse(fallback);
    }

    public int getInteger(String key, int fallback) {
        return parseInteger(map.get(key)).orElse(fallback);
    }

    public double getDouble(String key, double fallback) {
        return parseDouble(map.get(key)).orElse(fallback);
    }

    public String getString(String key, String fallback) {
        return parseString(map.get(key)).orElse(fallback);
    }

    public ResourceLocation getResourceLocation(String key, ResourceLocation fallback) {
        return parseResourceLocation(map.get(key)).orElse(fallback);
    }

    public List<ResourceLocation> getResourceLocationList(String keys, List<ResourceLocation> fallback) {
        return parseResourceLocationList(map.get(keys)).orElse(fallback);
    }

    public Potion getPotion(String key, Potion fallback) {
        return parsePotion(map.get(key)).orElse(fallback);
    }
}
