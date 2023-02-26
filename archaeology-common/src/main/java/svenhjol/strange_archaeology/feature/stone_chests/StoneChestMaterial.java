package svenhjol.strange_archaeology.feature.stone_chests;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import svenhjol.charm_api.iface.IVariantMaterial;

import java.util.Locale;

public enum StoneChestMaterial implements IVariantMaterial {
    STONE(MaterialColor.STONE),
    CHISELED_STONE(MaterialColor.STONE),
    DEEPSLATE(MaterialColor.DEEPSLATE),
    BLACKSTONE(MaterialColor.COLOR_BLACK),
    PRISMARINE(MaterialColor.COLOR_CYAN),
    PURPUR(MaterialColor.COLOR_PURPLE);
    
    private final MaterialColor materialColor;
    
    StoneChestMaterial(MaterialColor materialColor) {
        this.materialColor = materialColor;
    }
        
    @Override
    public Material material() {
        return Material.STONE;
    }

    @Override
    public MaterialColor materialColor() {
        return materialColor;
    }

    @Override
    public SoundType soundType() {
        return SoundType.STONE;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
