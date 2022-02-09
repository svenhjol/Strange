package svenhjol.strange.module.journals.screen.knowledge;

import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.discoveries.Discovery;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.PageTracker;
import svenhjol.strange.module.journals.helper.JournalClientHelper;
import svenhjol.strange.module.journals.paginator.BasePaginator;
import svenhjol.strange.module.journals.paginator.DiscoveryPaginator;
import svenhjol.strange.module.journals.screen.JournalPaginatedScreen;
import svenhjol.strange.module.journals.screen.JournalResources;

import java.util.function.Consumer;

public class JournalDiscoveriesScreen extends JournalPaginatedScreen<Discovery> {
    private final boolean withIgnored;
    private boolean hasIgnored;

    public JournalDiscoveriesScreen() {
        this(false);
    }

    public JournalDiscoveriesScreen(boolean withIgnored) {
        super(JournalResources.DISCOVERIES);
        this.withIgnored = withIgnored;
        this.hasIgnored = false;
    }

    @Override
    protected void init() {
        hasIgnored = JournalClientHelper.hasIgnoredAnyDiscoveries();
        super.init();
    }

    @Override
    protected Consumer<Discovery> onClick() {
        return discovery -> minecraft.setScreen(new JournalDiscoveryScreen(discovery));
    }

    @Override
    protected BasePaginator<Discovery> getPaginator() {
        var discoveries = JournalClientHelper.getFilteredDiscoveries(withIgnored);
        var paginator = new DiscoveryPaginator(discoveries);
        paginator.setButtonWidth(200);
        paginator.setOnItemButtonRendered(
            (discovery, button) -> JournalsClient.getJournal().ifPresent(
                journal -> {
                    if (journal.getIgnoredDiscoveries().contains(discovery.getRunes())) {
                        button.setAlpha(0.5F);
                    }
        }));
        return paginator;
    }

    @Override
    protected void addButtons() {
        bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> knowledge(), JournalResources.GO_BACK));

        if (!withIgnored && hasIgnored) {
            bottomButtons.add(1, new GuiHelper.ButtonDefinition(b -> discoveries(true), JournalResources.WITH_IGNORED));
        }
    }

    @Override
    protected void setViewedPage() {
        JournalsClient.tracker.setPage(PageTracker.Page.DISCOVERIES, offset);
    }
}
