package svenhjol.strange.module.journals.photo;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.bookmarks.Bookmark;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class BookmarkPhoto {
    private final Minecraft client;
    private final Bookmark bookmark;
    private ResourceLocation registeredTexture;

    public BookmarkPhoto(Minecraft client, Bookmark bookmark) {
        this.client = client;
        this.bookmark = bookmark;

        try {
            File photoFile = getPhotoFile();
            if (!photoFile.exists()) return;

            RandomAccessFile raf = new RandomAccessFile(photoFile, "r");
            //noinspection ConstantConditions
            if (raf != null) raf.close();

            InputStream stream = new FileInputStream(photoFile);
            NativeImage photo = NativeImage.read(stream);
            DynamicTexture photoTexture = new DynamicTexture(photo);
            registeredTexture = client.getTextureManager().register("photo", photoTexture);
            stream.close();

            if (registeredTexture == null) {
                throw new Exception("Null problems with texture / registered texture");
            }

        } catch (Exception e) {

            LogHelper.warn(getClass(), "Error loading photo: " + e);

        }
    }

    @Nullable
    public ResourceLocation getTexture() {
        return registeredTexture;
    }

    public boolean isValid() {
        return registeredTexture != null;
    }

    protected File getPhotoFile() {
        var screenshotsDirectory = new File(client.gameDirectory, "screenshots");
        var filename = "strange_" + bookmark.getRunes() + ".png";
        return new File(screenshotsDirectory, filename);
    }
}
