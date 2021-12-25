package svenhjol.strange.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;

public class StrangeCommands {
    public final static List<LiteralArgumentBuilder<CommandSourceStack>> SUBCOMMANDS = new ArrayList<>();

    public static void init() {
        CommandRegistrationCallback.EVENT.register(StrangeCommands::register);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(Strange.MOD_ID);

        for (LiteralArgumentBuilder<CommandSourceStack> subcommand : SUBCOMMANDS) {
            builder.then(subcommand);
        }

        dispatcher.register(builder);
        LogHelper.debug(Strange.MOD_ID, StrangeCommands.class, "Registered " + SUBCOMMANDS.size() + " subcommands");
    }
}
