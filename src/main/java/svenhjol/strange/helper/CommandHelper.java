package svenhjol.strange.helper;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.chat.TextComponent;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.Strange;

public class CommandHelper {
    public static CommandSyntaxException makeException(String type, String message, Object... args) {
        LogHelper.info(Strange.MOD_ID, CommandHelper.class, message);
        return new CommandSyntaxException(
            new SimpleCommandExceptionType(new TextComponent(type)),
            new TextComponent(String.format(message, args))
        );
    }
}
