package svenhjol.strange.module.journals.screen.knowledge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.discoveries.Discovery;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.screen.JournalResources;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.runes.client.RuneStringRenderer;

public class JournalDiscoveryScreen extends JournalScreen {
    protected final Discovery discovery;
    protected RuneStringRenderer runeStringRenderer;

    public JournalDiscoveryScreen(Discovery discovery) {
        super(new TextComponent(StringHelper.snakeToPretty(discovery.getLocation().getPath(), true)));
        this.discovery = discovery;
    }

    @Override
    protected void init() {
        super.init();
        var len = discovery.getRunes().length();
        int left = Math.min(len, 12) * 6;

        runeStringRenderer = new RuneStringRenderer(midX - left, 80, 13, 15, 12, 3);
        bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> discoveries(), JournalResources.GO_BACK));

        // Add ignore/unignore button.
        JournalsClient.getJournal().ifPresent(journal -> {
            GuiHelper.ImageButtonDefinition button;

            if (journal.getIgnoredDiscoveries().contains(discovery.getRunes())) {
                button = new GuiHelper.ImageButtonDefinition(b -> unignore(), JournalResources.NAVIGATION, 0, 72, 18, JournalResources.UNIGNORE_TOOLTIP);
            } else {
                button = new GuiHelper.ImageButtonDefinition(b -> ignore(), JournalResources.NAVIGATION, 20, 0, 18, JournalResources.IGNORE_TOOLTIP);
            }

            bottomNavButtons.add(button);
        });
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        renderDay(poseStack);
        renderRunes(poseStack);
    }

    protected void renderDay(PoseStack poseStack) {
        var time = discovery.getTime();
        if (time != 0) {
            var day = time / 24000L;
            var component = new TranslatableComponent("gui.strange.journal.discovered_day", day);
            GuiHelper.drawCenteredString(poseStack, font, component, midX, 34, secondaryColor | 0x303030);
        }
    }

    protected void renderRunes(PoseStack poseStack) {
        var runes = discovery.getRunes();
        runeStringRenderer.render(poseStack, font, runes);
    }

    protected void ignore() {
        JournalsClient.CLIENT_SEND_IGNORE_DISCOVERY.send(discovery);
    }

    protected void unignore() {
        JournalsClient.CLIENT_SEND_UNIGNORE_DISCOVERY.send(discovery);
    }
}
