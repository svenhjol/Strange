package svenhjol.strange.feature.runestones.client;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.charmony.feature.FeatureResolver;
import svenhjol.strange.api.impl.RunestoneLocation;
import svenhjol.strange.feature.core.client.BaseToast;
import svenhjol.strange.feature.runestones.RunestonesClient;
import svenhjol.strange.feature.runestones.common.Helpers;

public class TeleportedLocationToast extends BaseToast implements FeatureResolver<RunestonesClient> {
    public static final Component TITLE = 
        Component.translatable("toast.strange.runestones.teleported_location.title");
    
    public final Component description;
    
    public TeleportedLocationToast(RunestoneLocation location) {
        description = Component.translatable(
            "toast.strange.runestones.teleported_location.description",
            Component.translatable(Helpers.localeKey(location)));
    }
    
    @Override
    protected Component title() {
        return TITLE;
    }

    @Override
    protected Component description() {
        return description;
    }

    @Override
    protected ItemStack icon() {
        return new ItemStack(feature().linked().registers.stoneBlock.get());
    }

    @Override
    protected long duration() {
        return 9000L; // A bit longer to give time to read the location name.
    }

    @Override
    protected int color() {
        return 0x909090;
    }

    @Override
    public Class<RunestonesClient> typeForFeature() {
        return RunestonesClient.class;
    }
}
