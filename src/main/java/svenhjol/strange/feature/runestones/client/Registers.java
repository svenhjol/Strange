package svenhjol.strange.feature.runestones.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import svenhjol.charm.charmony.event.HudRenderEvent;
import svenhjol.charm.charmony.event.PlayerTickEvent;
import svenhjol.charm.charmony.feature.RegisterHolder;
import svenhjol.strange.feature.runestones.RunestonesClient;
import svenhjol.strange.feature.runestones.common.Networking;

import java.util.ArrayList;
import java.util.Collections;

public final class Registers extends RegisterHolder<RunestonesClient> {
    private static final ResourceLocation ILLAGER_GLYPHS = ResourceLocation.withDefaultNamespace("illageralt");

    public final Style runeFont;
    public final HudRenderer hudRenderer;

    public Registers(RunestonesClient feature) {
        super(feature);
        var registry = feature.registry();
        var linked = feature.linked();

        registry.blockRenderType(linked.registers.stoneBlock, RenderType::cutout);
        registry.blockRenderType(linked.registers.blackstoneBlock, RenderType::cutout);
        registry.blockRenderType(linked.registers.obsidianBlock, RenderType::cutout);

        // Attach the block entity renderer to the runestone block entity.
        registry.blockEntityRenderer(linked.registers.blockEntity, () -> RunestoneRenderer::new);

        // Receivers of packets sent by the server
        registry.packetReceiver(Networking.S2CWorldSeed.TYPE,
            () -> feature.handlers::worldSeedReceived);
        registry.packetReceiver(Networking.S2CActivationWarmup.TYPE,
            () -> feature.handlers::sacrificePositionReceived);
        registry.packetReceiver(Networking.S2CActivation.TYPE,
            () -> feature.handlers::activateRunestoneReceived);
        registry.packetReceiver(Networking.S2CTeleportedLocation.TYPE,
            () -> feature.handlers::teleportedLocationReceived);

        runeFont = Style.EMPTY.withFont(ILLAGER_GLYPHS);
        hudRenderer = new HudRenderer(feature);
    }

    @Override
    public void onEnabled() {
        var registry = feature().registry();
        var linked = feature().linked();

        HudRenderEvent.INSTANCE.handle(feature().handlers::hudRender);
        PlayerTickEvent.INSTANCE.handle(feature().handlers::playerTick);

        // Add items to the menu if feature is enabled.
        var blockItems = new ArrayList<>(linked.registers.blockItems);
        Collections.reverse(blockItems);

        for (var blockItem : blockItems) {
            registry.itemTab(
                blockItem.get(),
                CreativeModeTabs.FUNCTIONAL_BLOCKS,
                Items.LODESTONE
            );
        }
    }
}
