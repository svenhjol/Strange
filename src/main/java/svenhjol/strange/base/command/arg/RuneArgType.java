package svenhjol.strange.base.command.arg;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import svenhjol.strange.base.command.StrangeCommand;

import java.util.regex.Pattern;

public class RuneArgType implements ArgumentType<Character> {
    @Override
    public Character parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String raw = reader.getString().substring(cursor);

        if (raw.length() != 1) {
            reader.setCursor(cursor);
            throw StrangeCommand.makeException("Invalid letter", "%s is not a valid length", raw);
        }

        if (!Pattern.compile("[a-z]").matcher(raw).find()) {
            throw StrangeCommand.makeException("Invalid letter", "%s is not a valid letter", raw);
        }

        raw = raw.toLowerCase();
        reader.setCursor(reader.getString().length());
        return raw.charAt(0);
    }

    public static ArgumentType<Character> letter() {
        return new RuneArgType();
    }

    public static <S> Character getLetter(CommandContext<S> context, String name) {
        return context.getArgument(name, Character.class);
    }
}