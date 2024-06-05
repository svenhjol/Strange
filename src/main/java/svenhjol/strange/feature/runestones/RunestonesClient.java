package svenhjol.strange.feature.runestones;

import svenhjol.charm.charmony.annotation.Configurable;
import svenhjol.charm.charmony.annotation.Feature;
import svenhjol.charm.charmony.client.ClientFeature;
import svenhjol.charm.charmony.client.ClientLoader;
import svenhjol.charm.charmony.feature.LinkedFeature;
import svenhjol.strange.feature.runestones.client.Handlers;
import svenhjol.strange.feature.runestones.client.Registers;

@Feature
public final class RunestonesClient extends ClientFeature implements LinkedFeature<Runestones> {
    public final Registers registers;
    public final Handlers handlers;

    @Configurable(
        name = "Dim rune display background",
        description = "If true, focusing on a runestone will dim the background to make the text clearer."
    )
    private static boolean hudHasBackground = true;

    @Configurable(
        name = "Text shadow on rune display",
        description = "If true, adds a text shadow when focusing on a runestone."
    )
    private static boolean hudHasShadowText = true;

    public RunestonesClient(ClientLoader loader) {
        super(loader);

        registers = new Registers(this);
        handlers = new Handlers(this);
    }

    @Override
    public Class<Runestones> typeForLinked() {
        return Runestones.class;
    }

    public boolean hudHasBackground() {
        return hudHasBackground;
    }

    public boolean hudHasShadowText() {
        return hudHasShadowText;
    }
}
