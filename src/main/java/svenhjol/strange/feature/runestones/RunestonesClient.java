package svenhjol.strange.feature.runestones;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony_api.event.HudRenderEvent;
import svenhjol.charmony_api.event.PlayerTickEvent;
import svenhjol.strange.feature.runestones.RunestonesNetwork.SentLevelSeed;

import java.util.ArrayList;
import java.util.Collections;

public class RunestonesClient extends ClientFeature {
    public static final ResourceLocation ILLAGER_GLYPHS = new ResourceLocation("minecraft", "illageralt");
    public static final Style ILLAGER_GLYPHS_STYLE = Style.EMPTY.withFont(ILLAGER_GLYPHS);

    static RunestoneHudRenderer hudRenderer;
    static long seed;
    static boolean hasReceivedSeed = false;

    @Override
    public void register() {
        hudRenderer = new RunestoneHudRenderer();
    }

    @Override
    public void runWhenEnabled() {
        var registry = mod().registry();

        var blockItems = new ArrayList<>(Runestones.BLOCK_ITEMS);
        Collections.reverse(blockItems);

        for (var blockItem : blockItems) {
            registry.itemTab(blockItem, CreativeModeTabs.FUNCTIONAL_BLOCKS, Items.LODESTONE);
        }

        HudRenderEvent.INSTANCE.handle(this::handleHudRender);
        PlayerTickEvent.INSTANCE.handle(this::handlePlayerTick);
    }

    private void handleHudRender(GuiGraphics guiGraphics, float tickDelta) {
        hudRenderer.renderLabel(guiGraphics, tickDelta);
    }

    private void handlePlayerTick(Player player) {
        if (player.level().isClientSide) {
            hudRenderer.tick(player);
        }
    }

    public static void handleSendLevelSeed(SentLevelSeed message, Player player) {
        hasReceivedSeed = true;
        seed = message.getSeed();
        RunestoneHelper.CACHED_RUNIC_NAMES.clear();
    }
}
