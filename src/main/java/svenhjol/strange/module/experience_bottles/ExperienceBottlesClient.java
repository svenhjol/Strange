package svenhjol.strange.module.experience_bottles;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityType;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.api.event.ClientSpawnEntityCallback;
import svenhjol.charm.loader.CharmModule;

@ClientModule(module = ExperienceBottles.class)
public class ExperienceBottlesClient extends CharmModule {
    @Override
    public void register() {
        EntityRendererRegistry.register(ExperienceBottles.EXPERIENCE_BOTTLE, ThrownItemRenderer::new);
    }

    @Override
    public void runWhenEnabled() {
        ClientSpawnEntityCallback.EVENT.register(this::handleClientSpawnEntity);
    }

    private void handleClientSpawnEntity(ClientboundAddEntityPacket packet, EntityType<?> entityType, ClientLevel level, double x, double y, double z) {
        if (entityType == ExperienceBottles.EXPERIENCE_BOTTLE) {
            ClientSpawnEntityCallback.addEntity(packet, level, new ExperienceBottleEntity(level, x, y, z));
        }
    }
}
