package svenhjol.strange.base.helper;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;

public class VersionHelper {
    public static MainWindow getMainWindow(Minecraft mc) {
        return mc.mainWindow; // 1.14
        // return mc.getMainWindow(); // 1.15
    }
}
