package svenhjol.strange.module.journals2.paginator;

import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.module.journals.screen.knowledge.JournalDimensionScreen;

import java.util.List;
import java.util.function.Consumer;

public class DimensionPaginator extends ResourcePaginator {
    public DimensionPaginator(List<ResourceLocation> items) {
        super(items);
    }

    @Override
    protected Consumer<ResourceLocation> getItemClickAction(ResourceLocation item) {
        return i -> ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalDimensionScreen(i)));
    }
}
