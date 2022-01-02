package svenhjol.strange.module.runestones;

import net.minecraft.world.level.block.Blocks;
import svenhjol.charm.enums.ICharmEnum;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public enum RunestoneMaterial implements ICharmEnum {
    STONE(Properties.copy(Blocks.STONE)),
    DEEPSLATE(Properties.copy(Blocks.DEEPSLATE)),
    BLACKSTONE(Properties.copy(Blocks.BLACKSTONE)),
    NETHER_BRICK(Properties.copy(Blocks.NETHER_BRICKS)),
    OBSIDIAN(Properties.copy(Blocks.OBSIDIAN)),
    PURPUR(Properties.copy(Blocks.PURPUR_BLOCK));

    private final Properties properties;

    RunestoneMaterial(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public static List<RunestoneMaterial> getTypes() {
        return Arrays.stream(values()).collect(Collectors.toList());
    }

    @Nullable
    public static RunestoneMaterial byOrdinal(int o) {
        return Arrays.stream(values()).filter(m -> m.ordinal() == o).findFirst().orElse(null);
    }
}
