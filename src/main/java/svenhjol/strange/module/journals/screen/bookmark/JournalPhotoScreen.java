package svenhjol.strange.module.journals.screen.bookmark;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TextComponent;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.journals.photo.BookmarkPhoto;

@SuppressWarnings("ConstantConditions")
public class JournalPhotoScreen extends JournalScreen {
    protected Bookmark bookmark;
    protected BookmarkPhoto photo;

    public JournalPhotoScreen(Bookmark bookmark) {
        super(new TextComponent(bookmark.getName()));
        this.bookmark = bookmark;
    }

    @Override
    protected void init() {
        super.init();
        photo = new BookmarkPhoto(minecraft, bookmark);

        bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> back(), GO_BACK));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        if (photo.isValid()) {
            RenderSystem.setShaderTexture(0, photo.getTexture());
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
        if (photo.isValid()) {
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
