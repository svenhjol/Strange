package svenhjol.strange.module.runestones.definition;

import com.google.gson.Gson;
import net.minecraft.server.packs.resources.Resource;
import svenhjol.strange.module.knowledge.Knowledge;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a JSON file from data/(mod_id)/runestones/items/(namespace)(dimension).json.
 * It contains the IDs of the items that are consumed by runestones in order to teleport within the dimension.
 */
public class ItemDefinition {
    private final List<String> novice = new ArrayList<>();
    private final List<String> apprentice = new ArrayList<>();
    private final List<String> journeyman = new ArrayList<>();
    private final List<String> expert = new ArrayList<>();
    private final List<String> master = new ArrayList<>();

    public static ItemDefinition deserialize(Resource resource) {
        Reader reader = new InputStreamReader(resource.getInputStream());
        return new Gson().fromJson(reader, ItemDefinition.class);
    }

    public List<String> get(Knowledge.Tier tier) {
        return switch (tier) {
            case TEST -> List.of();
            case NOVICE -> novice;
            case APPRENTICE -> apprentice;
            case JOURNEYMAN -> journeyman;
            case EXPERT -> expert;
            case MASTER -> master;
        };
    }
}
