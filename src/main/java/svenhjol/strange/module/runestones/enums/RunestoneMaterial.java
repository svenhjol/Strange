package svenhjol.strange.module.runestones.enums;

import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public enum RunestoneMaterial implements IRunestoneMaterial {
    STONE(0, Properties.copy(Blocks.STONE)),
    DEEPSLATE(1, Properties.copy(Blocks.DEEPSLATE)),
    BLACKSTONE(2, Properties.copy(Blocks.BLACKSTONE)),
    NETHER_BRICK(3, Properties.copy(Blocks.NETHER_BRICKS)),
    OBSIDIAN(4, Properties.copy(Blocks.OBSIDIAN)),
    PURPUR(5, Properties.copy(Blocks.PURPUR_BLOCK));

    private final Properties properties;
    private final int id;

    RunestoneMaterial(int id, Properties properties) {
        this.id = id;
        this.properties = properties;
    }

    public int getId() {
        return id;
    }

    public Properties getProperties() {
        return properties;
    }

    public static List<RunestoneMaterial> getTypes() {
        return Arrays.stream(values()).collect(Collectors.toList());
    }

    @Nullable
    public static IRunestoneMaterial getById(int id) {
        return Arrays.stream(values()).filter(m -> m.id == id).findFirst().orElse(null);
    }
}
