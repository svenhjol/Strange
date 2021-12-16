package svenhjol.strange.module.runestones.definition;

import com.google.gson.Gson;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a JSON file from data/(mod_id)/runestones/destinations/(namespace)(dimension).json.
 * It contains the IDs of the structure and biomes that runestones should teleport to within the dimension.
 * Items at the top of the list will appear more commonly on runestones and consist of shorter and easier rune strings.
 */
public class DestinationDefinition {
    private final List<String> destinations = new ArrayList<>();

    public static DestinationDefinition deserialize(Resource resource) {
        Reader reader = new InputStreamReader(resource.getInputStream());
        return new Gson().fromJson(reader, DestinationDefinition.class);
    }

    public List<String> getDestinations() {
        return destinations;
    }
}
