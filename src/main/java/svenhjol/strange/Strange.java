package svenhjol.strange;

import svenhjol.charmony.base.DefaultMod;

public class Strange extends DefaultMod {
    public static final String MOD_ID = "strange";
    private static Strange instance;

    public static Strange instance() {
        if (instance == null) {
            instance = new Strange();
        }
        return instance;
    }

    @Override
    public String modId() {
        return MOD_ID;
    }
}
