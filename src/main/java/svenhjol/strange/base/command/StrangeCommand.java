package svenhjol.strange.base.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import svenhjol.strange.Strange;
import svenhjol.strange.base.command.arg.RuneArgType;
import svenhjol.strange.runestones.RunestoneHelper;

public class StrangeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal(Strange.MOD_ID)
            .then(CommandManager
                .literal("learnall")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(StrangeCommand::learnall))
            .then(CommandManager
                .literal("learnrune")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("letter", RuneArgType.letter())
                    .executes(StrangeCommand::learnrune))));
    }

    private static int learnall(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        for (int i = 0; i < 26; i++) {
            RunestoneHelper.addLearnedRune(player, i);
        }

        context.getSource().sendFeedback(new TranslatableText("runestone.strange.learned_all_runes"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int learnrune(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Character rune = RuneArgType.getLetter(context, "letter");

        int runeVal = (int)rune - 97;
        if (runeVal < 0 || runeVal > RunestoneHelper.NUMBER_OF_RUNES - 1)
            throw makeException("Invalid rune value", "Must be lowercase letter from a to z");

        ServerPlayerEntity player = context.getSource().getPlayer();
        RunestoneHelper.addLearnedRune(player, runeVal);

        context.getSource().sendFeedback(new TranslatableText("runestone.strange.learned_rune", rune), false);
        return Command.SINGLE_SUCCESS;
    }

    public static CommandSyntaxException makeException(String type, String message, Object... args) {
        return new CommandSyntaxException(
            new SimpleCommandExceptionType(new LiteralText(type)),
            new LiteralText(String.format(message, args))
        );
    }
}
