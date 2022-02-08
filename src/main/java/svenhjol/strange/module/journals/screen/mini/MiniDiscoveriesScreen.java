package svenhjol.strange.module.journals.screen.mini;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.TextComponent;
import svenhjol.strange.module.discoveries.DiscoveriesClient;
import svenhjol.strange.module.discoveries.DiscoveryHelper;
import svenhjol.strange.module.journals.helper.JournalClientHelper;
import svenhjol.strange.module.journals.paginator.DiscoveryPaginator;
import svenhjol.strange.module.journals.screen.JournalResources;
import svenhjol.strange.module.journals.screen.MiniJournal;
import svenhjol.strange.module.runes.client.RuneStringRenderer;

public class MiniDiscoveriesScreen extends BaseMiniScreen {
    private DiscoveryPaginator paginator;
    private RuneStringRenderer runeStringRenderer;

    public MiniDiscoveriesScreen(MiniJournal mini) {
        super(mini);
    }

    @Override
    public void init() {
        super.init();

        if (mini.selectedDiscovery != null) {
            runeStringRenderer = new RuneStringRenderer(journalMidX - 46, midY - 8, 9, 14, 10, 4);

            mini.addBackButton(b -> {
                mini.selectedDiscovery = null;
                mini.changeSection(MiniJournal.Section.DISCOVERIES);
            });

        } else {

            var branch = DiscoveriesClient.getBranch();
            if (branch.isEmpty()) return;

            var discoveries = JournalClientHelper.getFilteredDiscoveries(false);
            paginator = new DiscoveryPaginator(discoveries);
            setPaginatorDefaults(paginator);
            paginator.setButtonWidth(94);
            paginator.setOnItemHovered(discovery -> new TextComponent(DiscoveryHelper.getDiscoveryName(discovery)));

            paginator.init(screen, mini.offset, midX - 87, midY - 78, discovery -> {
                mini.selectedDiscovery = discovery;
                mini.redraw();
            }, newOffset -> {
                mini.offset = newOffset;
                mini.redraw();
            });

            mini.addBackButton(b -> mini.changeSection(MiniJournal.Section.HOME));

        }
    }

    @Override
    public void render(PoseStack poseStack, ItemRenderer itemRenderer, Font font) {

        if (mini.selectedDiscovery != null) {
            var name = DiscoveryHelper.getDiscoveryName(mini.selectedDiscovery);
            mini.renderTitle(poseStack, new TextComponent(name), midY - 94);
            runeStringRenderer.render(poseStack, font, mini.selectedDiscovery.getRunes());
        } else {
            mini.renderTitle(poseStack, JournalResources.DISCOVERIES, midY - 94);
            paginator.render(poseStack, itemRenderer, font);
        }
    }
}
