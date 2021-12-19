package svenhjol.strange.module.journals.screen.bookmark;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.module.journals.screen.JournalScreen;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;

@SuppressWarnings("ConstantConditions")
public abstract class JournalBaseBookmarkScreen extends JournalScreen {
    protected ResourceLocation registeredPhotoTexture = null;

    public JournalBaseBookmarkScreen(Component component) {
        super(component);
    }

    protected void initPhoto() {
        try {
            File photoFile = getPhoto();
            if (photoFile == null) {
                throw new Exception("Null problems with file");
            }

            RandomAccessFile raf = new RandomAccessFile(photoFile, "r");
            //noinspection ConstantConditions
            if (raf != null) raf.close();

            InputStream stream = new FileInputStream(photoFile);
            NativeImage photo = NativeImage.read(stream);
            DynamicTexture photoTexture = new DynamicTexture(photo);
            registeredPhotoTexture = minecraft.getTextureManager().register("photo", photoTexture);
            stream.close();

            if (registeredPhotoTexture == null) {
                throw new Exception("Null problems with texture / registered texture");
            }

        } catch (Exception e) {

            // TODO: move all this to renderable class
            //LogHelper.warn(getClass(), "Error loading photo: " + e);

        }
    }

    protected void renderPhoto(PoseStack poseStack) {
        // Let the subclass determine how to render and scale the photo.
    }

    @Nullable
    protected File getPhoto() {
        var screenshotsDirectory = new File(minecraft.gameDirectory, "screenshots");
        var filename = getPhotoFilename();
        if (filename.isEmpty() || !screenshotsDirectory.exists()) return null;

        return new File(screenshotsDirectory, filename);
    }

    protected String getPhotoFilename() {
        // Fill this out with the correct bookmark filename.
        return "";
    }

    protected boolean hasPhoto() {
        File photo = getPhoto();
        return photo != null && photo.exists();
    }
}
