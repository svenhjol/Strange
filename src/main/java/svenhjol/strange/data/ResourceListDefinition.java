package svenhjol.strange.data;

import com.google.gson.Gson;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;

/**
 * The ResourceListDefinition allows for:
 * - replace: this is the same as the vanilla minecraft functionality. The existing list will be replaced.
 * - interpolate: mutually exclusive with replace. The new list will be interwoven with the existing list.
 * - values: a list of items that will be reference by the resourcelocation.
 */
@SuppressWarnings({"unused", "RedundantThrows"})
public class ResourceListDefinition {
    private String replace;
    private String interpolate;
    private final LinkedList<String> values = new LinkedList<>();

    public boolean shouldReplace() {
        return this.replace != null && this.replace.equals("true");
    }

    public boolean shouldInterpolate() {
        return this.interpolate != null && this.interpolate.equals("true");
    }

    public LinkedList<String> getValues() {
        return values;
    }

    public static ResourceListDefinition deserialize(Resource resource) throws IOException {
        Reader reader = resource.openAsReader();
        return new Gson().fromJson(reader, ResourceListDefinition.class);
    }
}
