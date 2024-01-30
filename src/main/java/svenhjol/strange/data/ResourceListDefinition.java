package svenhjol.strange.data;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * The ResourceListDefinition allows for:
 * - replace: this is the same as the vanilla minecraft functionality. The existing list will be replaced.
 * - interpolate: mutually exclusive with replace. The new list will be interwoven with the existing list.
 * - required_features: a list of refs that represent charmony features to check.
 * - values: a list of refs that will be referenced by the resourcelocation.
 */
@SuppressWarnings({"unused", "RedundantThrows", "MismatchedQueryAndUpdateOfCollection"})
public class ResourceListDefinition {
    private String replace;
    private String interpolate;
    private final LinkedList<String> values = new LinkedList<>();
    private final LinkedList<String> required_features = new LinkedList<>();

    public boolean replace() {
        return this.replace != null && this.replace.equals("true");
    }

    public boolean interpolate() {
        return this.interpolate != null && this.interpolate.equals("true");
    }

    public LinkedList<ResourceLocation> values() {
        return values.stream()
            .map(ResourceLocation::tryParse)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    public LinkedList<ResourceLocation> requiredFeatures() {
        return required_features.stream()
            .map(ResourceLocation::tryParse)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    public static ResourceListDefinition deserialize(Resource resource) throws IOException {
        Reader reader = resource.openAsReader();
        return new Gson().fromJson(reader, ResourceListDefinition.class);
    }
}
