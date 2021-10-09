package svenhjol.strange.module.runestones.enums;

import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.world.level.block.state.BlockBehaviour.*;

public enum RunestoneMaterial implements IRunestoneMaterial {
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
}
