package svenhjol.strange.module.quests.command.arg;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import svenhjol.strange.helper.CommandHelper;

import java.util.regex.Pattern;

public class QuestDefinitionArgType implements ArgumentType<String> {
    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String raw = reader.getString().substring(cursor);

        if (!Pattern.compile("[a-z0-9_.]").matcher(raw).find()) {
            throw CommandHelper.makeException("Invalid definition", "%s is not a valid quest definition", raw);
        }

        reader.setCursor(reader.getString().length());
        return raw;
    }

    public static ArgumentType<String> definition() {
        return new QuestDefinitionArgType();
    }

    public static <S> String getDefinition(CommandContext<S> context, String name) {
        return context.getArgument(name, String.class);
    }
}
