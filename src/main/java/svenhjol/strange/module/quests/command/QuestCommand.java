package svenhjol.strange.module.quests.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.strange.helper.CommandHelper;
import svenhjol.strange.init.StrangeCommands;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.QuestData;
import svenhjol.strange.module.quests.Quests;
import svenhjol.strange.module.quests.command.arg.QuestDefinitionArgType;
import svenhjol.strange.module.quests.command.arg.QuestIdArgType;
import svenhjol.strange.module.quests.definition.QuestDefinition;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class QuestCommand {
    public static void init() {
        StrangeCommands.SUBCOMMANDS.addAll(Arrays.asList(
            Commands.literal("start_quest")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("definition", QuestDefinitionArgType.definition())
                    .suggests(QuestCommand::getQuestDefinitions)
                    .executes(QuestCommand::start)),

            Commands.literal("abandon_quest")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("id", QuestIdArgType.id())
                    .suggests(QuestCommand::getQuestIds)
                    .executes(QuestCommand::abandon)),

            Commands.literal("pause_quest")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("id", QuestIdArgType.id())
                    .suggests(QuestCommand::getQuestIds)
                    .executes(QuestCommand::pause)),

            Commands.literal("complete_quest")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("id", QuestIdArgType.id())
                    .suggests(QuestCommand::getQuestIds)
                    .executes(QuestCommand::complete))
        ));
    }

    private static int abandon(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        QuestData quests = getQuestData();

        String id = QuestIdArgType.getId(context, "id");
        Quest quest = quests.get(id);

        if (quest == null) {
            throw CommandHelper.makeException("Invalid quest", new TranslatableComponent("commands.strange.no_quest_found", id).getString());
        }

        quest.abandon(player);
        context.getSource().sendSuccess(new TranslatableComponent("commands.strange.abandoned_quest", id), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int pause(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        QuestData quests = getQuestData();

        String id = QuestIdArgType.getId(context, "id");
        Quest quest = quests.get(id);

        if (quest == null) {
            throw CommandHelper.makeException("Invalid quest", new TranslatableComponent("commands.strange.no_quest_found", id).getString());
        }

        quest.pause(player);
        context.getSource().sendSuccess(new TranslatableComponent("commands.strange.paused_quest", id), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int complete(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        QuestData quests = getQuestData();

        String id = QuestIdArgType.getId(context, "id");
        Quest quest = quests.get(id);

        if (quest == null) {
            throw CommandHelper.makeException("Invalid quest", new TranslatableComponent("commands.strange.no_quest_found", id).getString());
        }

        quest.complete(player, null);
        context.getSource().sendSuccess(new TranslatableComponent("commands.strange.completed_quest", id), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int start(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        QuestDefinition definition = Quests.getDefinition(QuestDefinitionArgType.getDefinition(context, "definition"));
        if (definition == null) {
            throw CommandHelper.makeException("Invalid definition", new TranslatableComponent("commands.strange.no_quest_definition").getString());
        }

        Quest quest = new Quest(definition, 1.0F);
        quest.start(player);
        context.getSource().sendSuccess(new TranslatableComponent("commands.strange.started_quest", quest.getId()), false);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Unwraps the optional, throws an exception if savedData is not available.
     */
    private static QuestData getQuestData() throws CommandSyntaxException {
        Optional<QuestData> quests = Quests.getQuestData();

        if (quests.isEmpty()) {
            throw CommandHelper.makeException("Internal error", new TranslatableComponent("commands.strange.no_quest_data").getString());
        }

        return quests.get();
    }

    private static CompletableFuture<Suggestions> getQuestIds(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<String> ids = new LinkedList<>();
        Quests.getQuestData().ifPresent(quests -> {
            List<Quest> all = quests.all();
            all.forEach(q -> ids.add(q.getId()));
        });
        return SharedSuggestionProvider.suggest(ids, builder);
    }

    private static CompletableFuture<Suggestions> getQuestDefinitions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<String> ids = new LinkedList<>();
        Quests.DEFINITIONS.forEach((tier, map) -> ids.addAll(map.keySet()));
        return SharedSuggestionProvider.suggest(ids, builder);
    }
}
