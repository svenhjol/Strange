package svenhjol.strange.feature.ebony_wood;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import svenhjol.charmony.api.iface.IVariantWoodMaterial;

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
    public BlockSetType blockSetType() {
        return EbonyWood.blockSetType.get();
    }

    @Override
    public WoodType woodType() {
        return EbonyWood.woodType.get();
    }

    @Override
    public SoundType soundType() {
        return SoundType.WOOD;
    }

    @Override
    public BlockBehaviour.Properties blockProperties() {
        return IVariantWoodMaterial.super.blockProperties()
            .noOcclusion();
    }
}
