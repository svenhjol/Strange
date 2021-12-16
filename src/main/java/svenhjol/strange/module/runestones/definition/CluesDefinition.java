package svenhjol.strange.module.runestones.definition;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CluesDefinition {
    public final List<String> safe = new ArrayList<>();
    public final List<String> underground = new ArrayList<>();
    public final List<String> underwater = new ArrayList<>();
    public final List<String> surface = new ArrayList<>();
    public final List<String> ancient = new ArrayList<>();
    public final List<String> dangerous = new ArrayList<>();
    public final List<String> lucrative = new ArrayList<>();

    public static CluesDefinition deserialize(Resource resource) {
        Reader reader = new InputStreamReader(resource.getInputStream());
        return new Gson().fromJson(reader, CluesDefinition.class);
    }

    public Map<ResourceLocation, List<String>> getAllClues() {
        Map<ResourceLocation, List<String>> map = new HashMap<>();

        addToMap("safe", safe, map);
        addToMap("underground", underground, map);
        addToMap("underwater", underwater, map);
        addToMap("surface", surface, map);
        addToMap("ancient", ancient, map);
        addToMap("dangerous", dangerous, map);
        addToMap("lucrative", lucrative, map);

        return map;
    }

    private void addToMap(String type, List<String> input, Map<ResourceLocation, List<String>> output) {
        input.stream().map(ResourceLocation::new).forEach(r -> output.computeIfAbsent(r, a -> new ArrayList<>()).add(type));
    }
}
