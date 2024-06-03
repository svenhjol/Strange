package svenhjol.strange.feature.runestones.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import svenhjol.charm.charmony.feature.RegisterHolder;
import svenhjol.strange.feature.runestones.RunestonesClient;
import svenhjol.strange.feature.runestones.common.Networking;

import java.util.ArrayList;
import java.util.Collections;

public final class Registers extends RegisterHolder<RunestonesClient> {
    public Registers(RunestonesClient feature) {
        super(feature);
        var registry = feature.registry();
        var linked = feature.linked();

        registry.blockRenderType(linked.registers.stoneBlock, RenderType::cutout);
        registry.blockRenderType(linked.registers.blackstoneBlock, RenderType::cutout);
        registry.blockRenderType(linked.registers.obsidianBlock, RenderType::cutout);

        // Attach the block entity renderer to the runestone block entity.
        registry.blockEntityRenderer(linked.registers.blockEntity, () -> RunestoneRenderer::new);

        // Client packet receivers.
        registry.packetReceiver(Networking.S2CSacrificeInProgress.TYPE,
            () -> feature.handlers::sacrificePositionReceived);
        registry.packetReceiver(Networking.S2CActivateRunestone.TYPE,
            () -> feature.handlers::activateRunestoneReceived);
    }

    @Override
    public void onEnabled() {
        var registry = feature().registry();
        var linked = feature().linked();

        // Add items to the menu if feature is enabled.
        var blockItems = new ArrayList<>(linked.registers.blockItems);
        Collections.reverse(blockItems);

        for (var blockItem : blockItems) {
            registry.itemTab(
                blockItem,
                CreativeModeTabs.FUNCTIONAL_BLOCKS,
                Items.LODESTONE
            );
        }
    }
}
