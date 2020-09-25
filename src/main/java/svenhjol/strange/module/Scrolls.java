package svenhjol.strange.module;

import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.event.LoadWorldCallback;
import svenhjol.meson.iface.Module;
import svenhjol.strange.item.ScrollItem;
import svenhjol.strange.mixin.accessor.MinecraftServerAccessor;
import svenhjol.strange.scroll.ScrollDefinition;

import javax.annotation.Nullable;
import java.util.*;

@Module(description = "Scrolls provide quest instructions and scrollkeeper villagers give rewards for completed scrolls.")
public class Scrolls extends MesonModule {
    public static final int MAX_TIERS = 6;
    public static Map<Integer, List<ScrollDefinition>> AVAILABLE_SCROLLS = new HashMap<>();
    public static Map<Integer, ScrollItem> SCROLL_TIERS = new HashMap<>();

    public static boolean useBuiltInScrolls = true;

    @Override
    public void init() {
        for (int i = 1; i <= MAX_TIERS; i++) {
            SCROLL_TIERS.put(i, new ScrollItem(this, i));
        }

        LoadWorldCallback.EVENT.register(this::tryLoadQuests);
    }

    private void tryLoadQuests(MinecraftServer server) {
        ResourceManager resources = ((MinecraftServerAccessor)server).getServerResourceManager().getResourceManager();

        for (int tier = 1; tier <= MAX_TIERS; tier++) {
            AVAILABLE_SCROLLS.put(tier, new ArrayList<>());
            Collection<Identifier> scrolls = resources.findResources("scrolls/tier" + tier, file -> file.endsWith(".json"));

            for (Identifier scroll : scrolls) {
                try {
                    ScrollDefinition definition = ScrollDefinition.deserialize(resources.getResource(scroll));

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
    public static ScrollDefinition getRandomDefinition(int tier, Random random) {
        if (!Scrolls.AVAILABLE_SCROLLS.containsKey(tier)) {
            Meson.LOG.warn("No scroll definitions available for this tier: " + tier);
            return null;
        }

        List<ScrollDefinition> definitions = AVAILABLE_SCROLLS.get(tier);
        return definitions.get(random.nextInt(definitions.size()));
    }
}
