package svenhjol.strange.module;

import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.event.LoadWorldCallback;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.client.ScrollsClient;
import svenhjol.strange.item.ScrollItem;
import svenhjol.strange.mixin.accessor.MinecraftServerAccessor;
import svenhjol.strange.scroll.JsonDefinition;

import javax.annotation.Nullable;
import java.util.*;

@Module(description = "Scrolls provide quest instructions and scrollkeeper villagers give rewards for completed scrolls.")
public class Scrolls extends MesonModule {
    public static final int MAX_TIERS = 6;
    public static final Identifier MSG_CLIENT_OPEN_SCROLL = new Identifier(Strange.MOD_ID, "client_open_scroll");
    public static Map<Integer, List<JsonDefinition>> AVAILABLE_SCROLLS = new HashMap<>();
    public static Map<Integer, ScrollItem> SCROLL_TIERS = new HashMap<>();
    public static Map<Integer, String> SCROLL_TIER_IDS = new HashMap<>();

    public ScrollsClient client;

    // TODO: make this a config
    public static boolean useBuiltInScrolls = true;

    // TODO: make this a config
    public static String language = "en";

    public Scrolls() {
        SCROLL_TIER_IDS.put(1, "novice");
        SCROLL_TIER_IDS.put(2, "apprentice");
        SCROLL_TIER_IDS.put(3, "journeyman");
        SCROLL_TIER_IDS.put(4, "expert");
        SCROLL_TIER_IDS.put(5, "master");
        SCROLL_TIER_IDS.put(6, "legendary");
    }

    @Override
    public void register() {
        for (int tier = 1; tier <= MAX_TIERS; tier++) {
            SCROLL_TIERS.put(tier, new ScrollItem(this, tier, "scroll_" + SCROLL_TIER_IDS.get(tier)));
        }
    }

    @Override
    public void init() {
        // load the scroll definitions when the world loads
        LoadWorldCallback.EVENT.register(this::tryLoadScrolls);
    }

    @Override
    public void clientInit() {
        this.client = new ScrollsClient(this);
    }

    private void tryLoadScrolls(MinecraftServer server) {
        ResourceManager resources = ((MinecraftServerAccessor)server).getServerResourceManager().getResourceManager();

        for (int tier = 1; tier <= MAX_TIERS; tier++) {
            AVAILABLE_SCROLLS.put(tier, new ArrayList<>());
            Collection<Identifier> scrolls = resources.findResources("scrolls/tier" + tier, file -> file.endsWith(".json"));

            for (Identifier scroll : scrolls) {
                try {
                    JsonDefinition definition = JsonDefinition.deserialize(resources.getResource(scroll));

                    // check that scroll definition is built-in and configured to be used
                    if (definition.isBuiltIn() && !useBuiltInScrolls)
                        continue;

                    // check that scroll modules are present and enabled
                    List<String> requiredModules = definition.getModules();
                    if (!requiredModules.isEmpty()) {
                        boolean valid = true;
                        for (String requiredModule : requiredModules) {
                            valid = valid && Meson.enabled(requiredModule);
                        }
                        if (!valid) {
                            Meson.LOG.info("Scroll definition " + scroll.toString() + " is missing required modules, disabling.");
                            continue;
                        }
                    }

                    String name = scroll.getPath().replace("/", ".").replace(".json", "");
                    definition.setTitle(name);
                    definition.setTier(tier);
                    AVAILABLE_SCROLLS.get(tier).add(definition);
                    Meson.LOG.info("Loaded scroll definition " + scroll.toString() + " for tier " + tier);

                } catch (Exception e) {
                    Meson.LOG.warn("Could not load scroll definition for " + scroll.toString() + " because " + e.getMessage());
                }
            }
        }
    }

    @Nullable
    public static JsonDefinition getRandomDefinition(int tier, Random random) {
        if (!Scrolls.AVAILABLE_SCROLLS.containsKey(tier)) {
            Meson.LOG.warn("No scroll definitions available for this tier: " + tier);
            return null;
        }

        List<JsonDefinition> definitions = AVAILABLE_SCROLLS.get(tier);
        if (definitions.isEmpty()) {
            Meson.LOG.warn("No scroll definitions found in this tier: " + tier);
            return null;
        }

        return definitions.get(random.nextInt(definitions.size()));
    }
}
