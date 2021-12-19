package svenhjol.strange.module.journals.screen.knowledge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.runes.RuneBranch;

import javax.annotation.Nullable;

public abstract class JournalResourceScreen extends JournalScreen {
    protected final ResourceLocation resource;

    public JournalResourceScreen(ResourceLocation resource) {
        super(new TextComponent(StringHelper.snakeToPretty(resource.getPath(), true)));
        this.resource = resource;
        setViewedPage();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        RuneBranch<?, ResourceLocation> branch = getBranch();
        if (branch == null) return;

        var runes = branch.get(resource);

        int left = midX - 74;
        int top = 80;
        int xOffset = 13;
        int yOffset = 15;

        renderRunesString(poseStack, runes, left, top, xOffset, yOffset, 12, 3, false);
    }

    @Nullable
    public abstract RuneBranch<?, ResourceLocation> getBranch();

    protected void setViewedPage() {
        // no op
    }
}
