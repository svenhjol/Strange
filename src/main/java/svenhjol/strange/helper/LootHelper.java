package svenhjol.strange.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import svenhjol.charm.helper.LogHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

// TODO: move to Charm
public class LootHelper {
    public static Optional<Map<Integer, List<Item>>> fetchItems(MinecraftServer server, ResourceLocation table) {
        Map<Integer, List<Item>> map = new HashMap<>();
        Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

        Resource resource;
        try {
            resource = server.getResourceManager().getResource(table);
        } catch (IOException e) {
            return Optional.empty();
        }

        InputStream inputStream = resource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        JsonObject obj = GsonHelper.fromJson(gson, reader, JsonObject.class);
        if (obj == null) return Optional.empty();
        JsonArray pools = GsonHelper.getAsJsonArray(obj, "pools");

        for (int p = 0; p < pools.size(); p++) {
            JsonArray elements = GsonHelper.getAsJsonArray((JsonObject) pools.get(p), "entries");
            for (int e = 0; e < elements.size(); e++) {
                JsonObject entry = (JsonObject) elements.get(e);
                String name = entry.get("name").getAsString();
                ResourceLocation res = new ResourceLocation(name);

                // try instantiate
                if (Registry.ITEM.getOptional(res).isPresent()) {
                    map.computeIfAbsent(p, a -> new LinkedList<>()).add(Registry.ITEM.get(res));
                } else {
                    LogHelper.debug(LootHelper.class, "Could not find item in registry: " + res);
                }
            }
        }

        return Optional.of(map);
    }
}
