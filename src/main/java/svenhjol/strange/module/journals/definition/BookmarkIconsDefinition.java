package svenhjol.strange.module.journals.definition;

import com.google.gson.Gson;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a JSON file from data/(mod_id)/journals/bookmark_icons.json.
 * It contains the item IDs of icons that are shown in the journal bookmark section.
 * The player can assign an icon from this list to any bookmark.
 */
public class BookmarkIconsDefinition {
    private final List<String> icons = new ArrayList<>();

    public static BookmarkIconsDefinition deserialize(Resource resource) {
        Reader reader = new InputStreamReader(resource.getInputStream());
        return new Gson().fromJson(reader, BookmarkIconsDefinition.class);
    }

    public List<String> getIcons() {
        return icons;
    }
}
