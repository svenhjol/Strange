package svenhjol.strange;

import svenhjol.charmony.base.DefaultClientMod;

public class StrangeClient extends DefaultClientMod {
    public static final String MOD_ID = "strange";
    private static StrangeClient instance;

    public static StrangeClient instance() {
        if (instance == null) {
            instance = new StrangeClient();
        }
        return instance;
    }

    @Override
    public String modId() {
        return MOD_ID;
    }
}
