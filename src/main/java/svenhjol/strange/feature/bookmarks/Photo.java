package svenhjol.strange.feature.bookmarks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import svenhjol.charmony.base.Mods;
import svenhjol.strange.Strange;

public class Photo {
    private final Bookmark bookmark;
    private int ticks;
    private boolean valid;
    private boolean isTakingPhoto;

    public Photo(Bookmark bookmark) {
        this.bookmark = bookmark;
        this.valid = true;
        this.isTakingPhoto = false;
    }

    public void tick() {
        ticks++;

        if (ticks < 60) {
            return;
        }

        if (ticks < 62) {
            hideGui();
            return;
        }

        if (ticks > 70) {
            // Escape if something is wrong.
            valid = false;
            return;
        }

        takePhoto();
    }

    public void finish() {
        showGui();
        valid = false;
    }

    public boolean isValid() {
        return valid;
    }

    public Bookmark getBookmark() {
        return bookmark;
    }

    private void takePhoto() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        if (isTakingPhoto) {
            return;
        }

        isTakingPhoto = true;

        net.minecraft.client.Screenshot.grab(
            minecraft.gameDirectory,
            bookmark.id + ".png",
            minecraft.getMainRenderTarget(),
            component -> {
                minecraft.player.playSound(Bookmarks.photoSound.get(), 1.0f, 1.0f);
                Mods.client(Strange.ID).log().debug(getClass(), "Photo taken");
                finish();
            }
        );
    }

    public void renderCountdown(GuiGraphics guiGraphics) {
        var minecraft = Minecraft.getInstance();
        int x = (guiGraphics.guiWidth() / 8) + 1;
        int y = 20;
        String str = "";

        if (ticks <= 20) {
            str = "3";
        } else if (ticks <= 40) {
            str = "2";
        } else if (ticks <= 60) {
            str = "1";
        }

        if (!str.isEmpty()) {
            var pose = guiGraphics.pose();
            pose.pushPose();
            pose.scale(4.0f, 4.0f, 1.0f);
            guiGraphics.drawCenteredString(minecraft.font, str, x, y, 0xffffff);
            pose.popPose();
        }
    }

    private void hideGui() {
        Minecraft.getInstance().options.hideGui = true;
    }

    private void showGui() {
        Minecraft.getInstance().options.hideGui = false;
    }

}
