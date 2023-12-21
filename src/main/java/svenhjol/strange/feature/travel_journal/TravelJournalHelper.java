package svenhjol.strange.feature.travel_journal;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.Strange;

import java.util.Locale;

public class TravelJournalHelper {
    public static String randomId() {
        return Strange.ID + "_" + RandomStringUtils.randomAlphanumeric(6).toLowerCase(Locale.ROOT);
    }

    public static ResourceLocation defaultItem(ResourceLocation dimension) {
        var itemRegistry = BuiltInRegistries.ITEM;

        return switch (dimension.toString()) {
            case "minecraft:the_nether" -> itemRegistry.getKey(Items.NETHERRACK);
            case "minecraft:the_end" -> itemRegistry.getKey(Items.END_STONE);
            default -> itemRegistry.getKey(Items.GRASS_BLOCK);
        };
    }

    public static String getPlayerBiomeLocaleKey(Player player) {
        var registry = player.level().registryAccess();
        var biome = player.level().getBiome(player.blockPosition());
        var key = registry.registryOrThrow(Registries.BIOME).getKey(biome.value());
        if (key == null) {
            throw new RuntimeException("Can't get player biome");
        }

        var namespace = key.getNamespace();
        var path = key.getPath();
        return "biome." + namespace + "." + path;
    }

    public static String getNiceDimensionName(ResourceLocation dim) {
        var path = dim.getPath();
        var pieces = path.split("_");
        pieces[0] = TextHelper.capitalize(pieces[0]);
        return String.join(" ", pieces);
    }

    public static ResourceLocation getPlayerDimension(Player player) {
        var level = player.level();
        var registry = level.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE);
        var key = registry.getKey(player.level().dimensionType());
        if (key == null) {
            throw new RuntimeException("Can't get player dimension");
        }

        return key;
    }
}
