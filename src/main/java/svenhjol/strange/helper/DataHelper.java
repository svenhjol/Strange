package svenhjol.strange.helper;

import net.minecraft.resources.ResourceLocation;
import svenhjol.charmony.base.Mods;

import java.util.List;

public class DataHelper {
    public static boolean hasRequiredFeatures(List<ResourceLocation> features) {
        return !features.isEmpty() && !features.stream().allMatch(
            f -> Mods.optionalCommon(f.getNamespace())
                .map(m -> m.loader().isEnabled(f.getPath()))
                .orElse(false));
    }
}
