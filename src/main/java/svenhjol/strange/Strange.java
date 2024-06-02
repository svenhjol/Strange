package svenhjol.strange;

import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.charmony.common.CommonFeature;

import java.util.List;

public final class Strange {
    public static final String ID = "strange";

    public static ResourceLocation id(String path) {
        return new ResourceLocation(ID, path);
    }

    public static List<Class<? extends CommonFeature>> features() {
        return List.of();
    }
}
