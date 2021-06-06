package svenhjol.strange.init;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import svenhjol.strange.command.StrangeCommand;
import svenhjol.strange.command.arg.QuestDefinitionArgType;
import svenhjol.strange.command.arg.QuestIdArgType;
import svenhjol.strange.command.arg.RuneArgType;

public class StrangeCommands {
    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            StrangeCommand.register(dispatcher);
        });

        ArgumentTypes.register("quest_definition", QuestDefinitionArgType.class, new EmptyArgumentSerializer<>(QuestDefinitionArgType::new));
        ArgumentTypes.register("quest_id", QuestIdArgType.class, new EmptyArgumentSerializer<>(QuestIdArgType::new));
        ArgumentTypes.register("rune", RuneArgType.class, new EmptyArgumentSerializer<>(RuneArgType::new));
    }
}
