package svenhjol.strange.base.command.arg;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import svenhjol.strange.base.command.StrangeCommand;

import java.util.regex.Pattern;

public class QuestIdArgType implements ArgumentType<String> {
    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String raw = reader.getString().substring(cursor);

        if (!Pattern.compile("[a-zA-Z0-9]").matcher(raw).find())
            throw StrangeCommand.makeException("Invalid ID", "%s is not a valid quest ID", raw);

        reader.setCursor(reader.getString().length());
        return raw;
    }

    public static ArgumentType<String> id() {
        return new QuestIdArgType();
    }

    public static <S> String getId(CommandContext<S> context, String name) {
        return context.getArgument(name, String.class);
    }
}