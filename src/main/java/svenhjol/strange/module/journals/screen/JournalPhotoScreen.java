package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.JournalBookmark;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;

@SuppressWarnings("ConstantConditions")
public class JournalPhotoScreen extends JournalScreen {
    protected JournalBookmark bookmark;
    protected DynamicTexture photoTexture = null;
    protected ResourceLocation registeredPhotoTexture = null;
    protected int photoFailureRetries;
    protected boolean hasPhoto;

    public JournalPhotoScreen(JournalBookmark bookmark) {
        super(new TextComponent(bookmark.getName()));

        this.bookmark = bookmark;
        this.photoFailureRetries = 0;
        this.bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> back(), GO_BACK));
    }

    @Override
    protected void init() {
        super.init();
        hasPhoto = hasPhoto();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        // render icon next to title
        renderTitleIcon(bookmark.getIcon());

        if (!hasPhoto || minecraft == null) {
            return;
        }

        if (photoTexture == null) {
            try {
                File photoFile = getPhoto();
                if (photoFile == null)
                    throw new Exception("Null problems with file");

                RandomAccessFile raf = new RandomAccessFile(photoFile, "r");
                if (raf != null)
                    raf.close();

                InputStream stream = new FileInputStream(photoFile);
                NativeImage photo = NativeImage.read(stream);
                photoTexture = new DynamicTexture(photo);
                registeredPhotoTexture = minecraft.getTextureManager().register("photo", photoTexture);
                stream.close();

                if (photoTexture == null || registeredPhotoTexture == null)
                    throw new Exception("Null problems with texture / registered texture");

            } catch (Exception e) {
                LogHelper.warn(this.getClass(), "Error loading photo: " + e);
                photoFailureRetries++;

                if (photoFailureRetries > 2) {
                    LogHelper.error(getClass(), "Failure loading photo, aborting retries");
                    hasPhoto = false;
                    registeredPhotoTexture = null;
                    photoTexture = null;
                }
            }
        }

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
     */
    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (hasPhoto && registeredPhotoTexture != null) {
            if (x > (midX - 107) && x < midX + 107
                && y > 42 && y < 175) {
                minecraft.setScreen(new JournalBookmarkScreen(bookmark));
                return true;
            }
        }

        return super.mouseClicked(x, y, button);
    }

    @Nullable
    protected File getPhoto() {
        if (minecraft == null) {
            return null;
        }

        File screenshotsDirectory = new File(minecraft.gameDirectory, "screenshots");
        return new File(screenshotsDirectory, bookmark.getId() + ".png");
    }

    private boolean hasPhoto() {
        File photo = getPhoto();
        return photo != null && photo.exists();
    }

    private void back() {
        if (minecraft != null) {
            minecraft.setScreen(new JournalBookmarkScreen(bookmark));
        }
    }
}
