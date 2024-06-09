package svenhjol.strange.feature.travel_journals.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class ClientHelpers {
    /**
     * Draw a centered string with optional dropshadow.
     * @see GuiGraphics#drawCenteredString 
     */
    public static void drawCenteredString(GuiGraphics guiGraphics, Font font, Component component, int x, int y, int color, boolean dropShadow) {
        var formattedCharSequence = component.getVisualOrderText();
        guiGraphics.drawString(font, formattedCharSequence, x - font.width(formattedCharSequence) / 2, y, color, dropShadow);
    }

    /**
     * Gets the nice name of the biome that the player is in.
     */
    public static String biomeName(Player player) {
        return Component.translatable(biomeLocaleKey(player)).getString();
    }

    /**
     * Get a locale key for the biome at the player's current position.
     */
    public static String biomeLocaleKey(Player player) {
        var registry = player.level().registryAccess();
        var biome = player.level().getBiome(player.blockPosition());
        var key = registry.registryOrThrow(Registries.BIOME).getKey(biome.value());

        if (key == null) {
            throw new RuntimeException("Cannot get player biome");
        }

        var namespace = key.getNamespace();
        var path = key.getPath();
        return "biome." + namespace + "." + path;
    }
}
