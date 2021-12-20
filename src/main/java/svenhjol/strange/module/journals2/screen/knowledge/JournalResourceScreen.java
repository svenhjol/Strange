package svenhjol.strange.module.journals2.screen.knowledge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.module.journals2.screen.JournalScreen;
import svenhjol.strange.module.runes.RuneBranch;
import svenhjol.strange.module.runes.client.RuneStringRenderer;

import javax.annotation.Nullable;

public abstract class JournalResourceScreen extends JournalScreen {
    protected RuneStringRenderer runeStringRenderer;
    protected final ResourceLocation resource;

    public JournalResourceScreen(ResourceLocation resource) {
        super(new TextComponent(StringHelper.snakeToPretty(resource.getPath(), true)));
        this.resource = resource;
        setViewedPage();
    }

    @Override
    protected void init() {
        super.init();
        runeStringRenderer = new RuneStringRenderer(midX - 74, 80, 13, 15, 12, 3);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        RuneBranch<?, ResourceLocation> branch = getBranch();
        if (branch == null) return;

        var runes = branch.get(resource);
        runeStringRenderer.render(poseStack, font, runes);
    }

    @Nullable
    public abstract RuneBranch<?, ResourceLocation> getBranch();

    protected void setViewedPage() {
        // no op
    }
}
