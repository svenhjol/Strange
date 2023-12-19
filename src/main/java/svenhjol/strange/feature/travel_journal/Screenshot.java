package svenhjol.strange.feature.travel_journal;

import net.minecraft.client.Minecraft;
import svenhjol.charmony.base.Mods;
import svenhjol.strange.Strange;

public class Screenshot {
    private final Bookmark bookmark;
    private int ticks;
    private boolean valid;
    private boolean isTakingScreenshot;

    public Screenshot(Bookmark bookmark) {
        this.bookmark = bookmark;
        this.valid = true;
        this.isTakingScreenshot = false;
        hideGui();
    }

    public void tick() {
        ticks++;

        if (ticks < 20) {
            return;
        }

        if (ticks > 30) {
            // Escape if something is wrong.
            valid = false;
            return;
        }

        takeScreenshot();
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

    private void takeScreenshot() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        if (isTakingScreenshot) {
            return;
        }

        isTakingScreenshot = true;

        net.minecraft.client.Screenshot.grab(
            minecraft.gameDirectory,
            bookmark.id + ".png",
            minecraft.getMainRenderTarget(),
            component -> {
                minecraft.player.playSound(TravelJournal.screenshotSound.get(), 1.0f, 1.0f);
                Mods.client(Strange.ID).log().debug(getClass(), "Screenshot taken");
                finish();
            }
        );
    }

    private void hideGui() {
        Minecraft.getInstance().options.hideGui = true;
    }

    private void showGui() {
        Minecraft.getInstance().options.hideGui = false;
    }
}
