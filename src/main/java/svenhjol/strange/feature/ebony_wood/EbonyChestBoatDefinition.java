package svenhjol.strange.feature.ebony_wood;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.ItemLike;
import svenhjol.charmony.feature.custom_wood.CustomWood;
import svenhjol.charmony_api.iface.IVariantChestBoatDefinition;
import svenhjol.charmony_api.iface.IVariantWoodMaterial;

import java.util.function.Supplier;

public class EbonyChestBoatDefinition implements IVariantChestBoatDefinition {
    @Override
    public Pair<Supplier<? extends ItemLike>, Supplier<? extends ItemLike>> getBoatPair() {
        var holder = CustomWood.getHolder(EbonyMaterial.EBONY).getBoat().orElseThrow();
        return Pair.of(holder.boat, holder.chestBoat);
    }

    @Override
    public IVariantWoodMaterial getMaterial() {
        return EbonyMaterial.EBONY;
    }
}
