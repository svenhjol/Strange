package svenhjol.strange.module.journals.screen.bookmark;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TextComponent;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.bookmarks.Bookmark;

@SuppressWarnings("ConstantConditions")
public class JournalPhotoScreen extends JournalBaseBookmarkScreen {
    protected Bookmark bookmark;

    public JournalPhotoScreen(Bookmark bookmark) {
        super(new TextComponent(bookmark.getName()));
        this.bookmark = bookmark;
    }

    @Override
    protected void init() {
        super.init();
        initPhoto();

        bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> back(), GO_BACK));
    }

    @Override
    protected String getPhotoFilename() {
        return "strange_" + bookmark.getRunes() + ".png";
    }

    @Override
    protected void renderPhoto(PoseStack poseStack) {
        if (registeredPhotoTexture != null) {
            RenderSystem.setShaderTexture(0, registeredPhotoTexture);
            poseStack.pushPose();
            poseStack.scale(0.825F, 0.66F, 0.825F);
            blit(poseStack, ((int)(midX / 0.825F) - 128), 65, 0, 0, 256, 200);
            poseStack.popPose();
        }
    }

    /**
     * Add an area of the page that allows photo to be clicked.
     * In this case, clicking the photo takes you back to the bookmark edit screen.
     */
    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (hasPhoto() && registeredPhotoTexture != null) {
            if (x > (midX - 107) && x < midX + 107
                && y > 42 && y < 175) {
                minecraft.setScreen(new JournalBookmarkScreen(bookmark));
                return true;
            }
        }

        return super.mouseClicked(x, y, button);
    }

    private void back() {
        minecraft.setScreen(new JournalBookmarkScreen(bookmark));
    }
}
