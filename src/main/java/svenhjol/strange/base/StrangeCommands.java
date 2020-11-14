package svenhjol.strange.base;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import svenhjol.strange.base.command.StrangeCommand;

public class StrangeCommands {
    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (!dedicated)
                StrangeCommand.register(dispatcher);
        });
    }
}
