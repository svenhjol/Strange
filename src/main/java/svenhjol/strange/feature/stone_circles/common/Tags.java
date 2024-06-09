package svenhjol.strange.feature.stone_circles.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import svenhjol.strange.Strange;

public final class Tags {
    public static final TagKey<Structure> ON_STONE_CIRCLE_MAPS = TagKey.create(Registries.STRUCTURE,
        Strange.id("on_stone_circle_maps"));
}
