package svenhjol.strange.feature.ebony_wood;

import svenhjol.charmony.iface.ICustomWoodDefinition;
import svenhjol.charmony.api.iface.IVariantWoodMaterial;

public class EbonyWoodDefinition implements ICustomWoodDefinition {
    @Override
    public IVariantWoodMaterial getMaterial() {
        return EbonyMaterial.EBONY;
    }

    @Override
    public boolean boat() { return true; }

    @Override
    public boolean button() { return true; }

    @Override
    public boolean door() { return true; }

    @Override
    public boolean fence() { return true; }

    @Override
    public boolean gate() { return true; }

    @Override
    public boolean hangingSign() { return true; }

    @Override
    public boolean leaves() {
        return true;
    }

    @Override
    public boolean log() { return true; }

    @Override
    public boolean planks() { return true; }

    @Override
    public boolean pressurePlate() { return true; }

    @Override
    public boolean sapling() {
        return true;
    }

    @Override
    public boolean sign() { return true; }

    @Override
    public boolean slab() { return true; }

    @Override
    public boolean stairs() { return true; }

    @Override
    public boolean trapdoor() { return true; }

    @Override
    public boolean wood() { return true; }
}
