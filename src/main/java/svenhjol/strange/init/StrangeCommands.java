package svenhjol.strange.init;

import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import svenhjol.strange.command.arg.RuneArgType;

public class StrangeCommands {
    public static void init() {
        ArgumentTypes.register("rune", RuneArgType.class, new EmptyArgumentSerializer<>(RuneArgType::new));
    }
}
