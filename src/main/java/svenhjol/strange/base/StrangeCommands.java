package svenhjol.strange.base;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import svenhjol.strange.base.command.StrangeCommand;
import svenhjol.strange.base.command.arg.QuestDefinitionArgType;
import svenhjol.strange.base.command.arg.QuestIdArgType;
import svenhjol.strange.base.command.arg.RuneArgType;

public class StrangeCommands {
    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            StrangeCommand.register(dispatcher);
        });

        ArgumentTypes.register("quest_definition", QuestDefinitionArgType.class, new ConstantArgumentSerializer<>(QuestDefinitionArgType::new));
        ArgumentTypes.register("quest_id", QuestIdArgType.class, new ConstantArgumentSerializer<>(QuestIdArgType::new));
        ArgumentTypes.register("rune", RuneArgType.class, new ConstantArgumentSerializer<>(RuneArgType::new));
    }
}
