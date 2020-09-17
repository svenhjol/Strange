package svenhjol.strange.module;

import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.event.LoadWorldCallback;
import svenhjol.meson.iface.Module;
import svenhjol.strange.mixin.accessor.MinecraftServerAccessor;
import svenhjol.strange.quest.QuestDefinition;

import java.util.*;

@Module(description = "Quests are written on scrolls and scrollkeeper villagers provide rewards for completed quests.")
public class Quests extends MesonModule {
    public static final int MAX_TIERS = 6;
    public static Map<Integer, List<QuestDefinition>> AVAILABLE_QUESTS = new HashMap<>();

    public static boolean useBuiltInQuests = true;

    @Override
    public void init() {
        LoadWorldCallback.EVENT.register(this::tryLoadQuests);
    }

    private void tryLoadQuests(MinecraftServer server) {
        ResourceManager resources = ((MinecraftServerAccessor)server).getServerResourceManager().getResourceManager();

        for (int tier = 1; tier <= MAX_TIERS; tier++) {
            AVAILABLE_QUESTS.put(tier, new ArrayList<>());
            Collection<Identifier> quests = resources.findResources("quests/tier" + tier, file -> file.endsWith(".json"));

            for (Identifier quest : quests) {
                try {
                    QuestDefinition definition = QuestDefinition.deserialize(resources.getResource(quest));

                    // check that quest module is built-in and configured to be used
                    if (definition.isBuiltIn() && !useBuiltInQuests)
                        continue;

                    // check that quest modules are present and enabled
                    List<String> requiredModules = definition.getModules();
                    if (!requiredModules.isEmpty()) {
                        boolean valid = true;
                        for (String requiredModule : requiredModules) {
                            valid = valid && Meson.enabled(requiredModule);
                        }
                        if (!valid) {
                            Meson.LOG.info("Quest " + quest.toString() + " is missing required modules, disabling.");
                            continue;
                        }
                    }

                    String name = quest.getPath().replace("/", ".").replace(".json", "");
                    definition.setTitle(name);
                    definition.setTier(tier);
                    AVAILABLE_QUESTS.get(tier).add(definition);
                    Meson.LOG.info("Loaded quest " + quest.toString() + " for tier " + tier);


                } catch (Exception e) {
                    Meson.LOG.warn("Could not load quest for " + quest.toString() + " because " + e.getMessage());
                }
            }
        }
    }
}
