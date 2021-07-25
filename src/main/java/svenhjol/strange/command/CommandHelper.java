package svenhjol.strange.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.chat.TextComponent;

public class CommandHelper {
    public static CommandSyntaxException makeException(String type, String message, Object... args) {
        return new CommandSyntaxException(
            new SimpleCommandExceptionType(new TextComponent(type)),
            new TextComponent(String.format(message, args))
        );
    }
}
