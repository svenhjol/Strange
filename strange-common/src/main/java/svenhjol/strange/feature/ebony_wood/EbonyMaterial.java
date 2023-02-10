package svenhjol.strange.feature.ebony_wood;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import svenhjol.charm_api.iface.IVariantMaterial;

import java.util.Locale;

public enum EbonyMaterial implements IVariantMaterial {
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
    public SoundType soundType() {
        return SoundType.WOOD;
    }
}
