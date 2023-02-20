package svenhjol.strange.feature.ebony_wood;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import svenhjol.charm_api.iface.IVariantWoodMaterial;

import java.util.Locale;

public enum EbonyMaterial implements IVariantWoodMaterial {
    EBONY;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public boolean isFlammable() {
        return true;
    }

    @Override
    public Material material() {
        return Material.WOOD;
    }

    @Override
    public MaterialColor materialColor() {
        return MaterialColor.COLOR_GRAY;
    }

    @Override
    public int chestBoatColor() {
        return 0x595960;
    }

    @Override
    public BlockSetType getBlockSetType() {
        return EbonyWood.BLOCK_SET_TYPE.get();
    }

    @Override
    public WoodType getWoodType() {
        return EbonyWood.WOOD_TYPE.get();
    }

    @Override
    public SoundType soundType() {
        return SoundType.WOOD;
    }
}
