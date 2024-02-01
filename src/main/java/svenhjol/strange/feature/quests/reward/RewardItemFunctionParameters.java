package svenhjol.strange.feature.quests.reward;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import svenhjol.strange.data.SimpleObjectParser;

import java.util.HashMap;
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

    @Override
    public String namespace() {
        return functionDefinition().namespace();
    }

    public boolean getBoolean(String key, boolean def) {
        return parseBoolean(map.getOrDefault(key, def));
    }

    public int getInteger(String key, int def) {
        return parseInteger(map.getOrDefault(key, def));
    }

    public double getDouble(String key, double def) {
        return parseDouble(map.getOrDefault(key, def));
    }

    public String getString(String key, String def) {
        return parseString(map.getOrDefault(key, def));
    }

    public ResourceLocation getResourceLocation(String key, ResourceLocation def) {
        return parseResourceLocation(map.getOrDefault(key, def));
    }
}
