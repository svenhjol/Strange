package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.data.JournalLocation;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;

@SuppressWarnings("ConstantConditions")
public class JournalPhotoScreen extends BaseJournalScreen {
    protected JournalLocation location;
    protected DynamicTexture photoTexture = null;
    protected ResourceLocation registeredPhotoTexture = null;
    protected int photoFailureRetries = 0;
    protected boolean hasPhoto;

    public JournalPhotoScreen(JournalLocation location) {
        super(new TextComponent(location.getName()));

        this.location = location;

        bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> back(),
            new TranslatableComponent("gui.strange.journal.go_back")));
    }

    @Override
    protected void init() {
        super.init();

        this.hasPhoto = hasPhoto();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        // render icon next to title
        renderTitleIcon(location.getIcon());

        if (!hasPhoto || minecraft == null)
            return;

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
            blit(poseStack, ((int)((width / 2) / 0.825F) - 128), 65, 0, 0, 256, 200);

            poseStack.popPose();
        }
    }

    /**
     * Add an area of the page that allows photo to be clicked.
     */
    @Override
    public boolean mouseClicked(double x, double y, int button) {
        int mid = width / 2;

        if (hasPhoto && registeredPhotoTexture != null) {
            if (x > (mid - 107) && x < mid + 107
                && y > 42 && y < 175) {
                minecraft.setScreen(new JournalLocationScreen(location));
                return true;
            }
        }

        return super.mouseClicked(x, y, button);
    }

    @Nullable
    protected File getPhoto() {
        if (minecraft == null)
            return null;

        File screenshotsDirectory = new File(minecraft.gameDirectory, "screenshots");
        return new File(screenshotsDirectory, location.getId() + ".png");
    }

    private boolean hasPhoto() {
        File photo = getPhoto();
        return photo != null && photo.exists();
    }

    private void back() {
        if (minecraft != null)
            minecraft.setScreen(new JournalLocationScreen(location));
    }
}
