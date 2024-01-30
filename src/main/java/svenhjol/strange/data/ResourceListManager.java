package svenhjol.strange.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.iface.ILog;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ResourceListManager {
    private static final Map<String, Map<ResourceLocation, LinkedList<ResourceLocation>>> CACHED_ENTRIES = new HashMap<>();

    /**
     * Assemble map of resourcelocations -> lists.
     */
    public static Map<ResourceLocation, LinkedList<ResourceLocation>> entries(ResourceManager manager, String folder) {
        var cached = CACHED_ENTRIES.get(folder);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        Map<ResourceLocation, LinkedList<ResourceLocation>> map = new HashMap<>();

        var files = manager.listResources(folder, f -> f.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : files.entrySet()) {
            var key = entry.getKey();
            var resource = entry.getValue();

            var path = key.getPath()
                .replace(folder, "")
                .replace("/", "")
                .replace(".json", "");

            var id = new ResourceLocation(key.getNamespace(), path);

            try {
                var definition = ResourceListDefinition.deserialize(resource);
                var entries = definition.getValues().stream().map(ResourceLocation::new).toList();

                if (definition.shouldReplace()) {
                    map.put(id, new LinkedList<>());
                }

                var list = map.computeIfAbsent(id, m -> new LinkedList<>());

                if (definition.shouldInterpolate()) {
                    // Interpolate the new entries into the existing list.
                    var existing = new LinkedList<>(list);
                    list.clear();

                    var biggest = Math.max(entries.size(), existing.size());
                    for (int i = 0; i < biggest; i++) {
                        if (i < existing.size()) {
                            list.add(existing.get(i));
                        }
                        if (i < entries.size()) {
                            list.add(entries.get(i));
                        }
                    }
                } else {
                    // Append the new entries to the existing list.
                    list.addAll(entries);
                }

                log().debug(ResourceListManager.class, "Added " + entries.size() + " new entries to " + id);
            } catch (Exception e) {
                log().warn(ResourceListManager.class, "Could not load resource list definition from " + key + ": " + e.getMessage());
            }
        }

        CACHED_ENTRIES.put(folder, map);
        return map;
    }

    public static void clearEntriesCache() {
        CACHED_ENTRIES.clear();;
    }

    private static ILog log() {
        return Mods.common(Strange.ID).log();
    }
}
