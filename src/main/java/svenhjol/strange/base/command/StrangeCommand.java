package svenhjol.strange.base.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import svenhjol.strange.Strange;
import svenhjol.strange.base.command.arg.RuneArgType;
import svenhjol.strange.base.helper.RunestoneHelper;
import svenhjol.strange.runestones.capability.IRunestonesCapability;
import svenhjol.strange.runestones.module.Runestones;

import java.util.ArrayList;
import java.util.function.Predicate;

public class StrangeCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal(Strange.MOD_ID)
            .then(Commands.literal("learnall")
                .requires(perm(Permissions.LEARN))
                .executes(StrangeCommand::learnall))
            .then(Commands.literal("learnrune")
                .requires(perm(Permissions.LEARN))
                .then(Commands.argument("letter", RuneArgType.letter())
                    .executes(StrangeCommand::learnrune))));

        PermissionAPI.registerNode(Permissions.LEARN, DefaultPermissionLevel.OP, "Allows use of the learn commands");
    }

    private static int learnall(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        IRunestonesCapability cap = Runestones.getCapability(player);

        for (int i = 0; i < 26; i++) {
            cap.discoverType(i);
        }

        context.getSource().sendFeedback(new StringTextComponent("Learned all runes"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int learnrune(CommandContext<CommandSource> context) throws CommandSyntaxException {
        Character rune = RuneArgType.getLetter(context, "letter");
        ArrayList<Character> values = new ArrayList<>(RunestoneHelper.getRuneCharMap().values());
        if (values.contains(rune)) {
            int learned = values.indexOf(rune);
            ServerPlayerEntity player = context.getSource().asPlayer();
            Runestones.getCapability(player).discoverType(learned);
        } else {
            throw makeException("Invalid rune value", "Could not learn the rune %s", rune);
        }

        context.getSource().sendFeedback(new StringTextComponent("Learned rune " + rune), false);
        return Command.SINGLE_SUCCESS;
    }

    public static CommandSyntaxException makeException(String type, String message, Object... args) {
        return new CommandSyntaxException(
            new SimpleCommandExceptionType(new StringTextComponent(type)),
            new StringTextComponent(String.format(message, args))
        );
    }

    private static Predicate<CommandSource> perm(String node) {
        return commandSource -> {
            try {
                return PermissionAPI.hasPermission(commandSource.asPlayer(), node);
            } catch (Throwable e) {
                return commandSource.hasPermissionLevel(2);
            }
        };
    }
}
