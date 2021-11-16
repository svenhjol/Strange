package svenhjol.strange.module.quests;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.helper.CommandHelper;
import svenhjol.strange.module.knowledge.KnowledgeCommand;
import svenhjol.strange.module.quests.command.arg.QuestDefinitionArgType;
import svenhjol.strange.module.quests.command.arg.QuestIdArgType;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class QuestCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(Strange.MOD_ID + "_quests")
            .then(Commands.literal("start")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("definition", QuestDefinitionArgType.definition())
                    .suggests(QuestCommand::getQuestDefinitions)
                    .executes(QuestCommand::start)))

            .then(Commands.literal("abandon")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("id", QuestIdArgType.id())
                    .suggests(QuestCommand::getQuestIds)
                    .executes(QuestCommand::abandon)))

            .then(Commands.literal("complete")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("id", QuestIdArgType.id())
                    .suggests(QuestCommand::getQuestIds)
                    .executes(QuestCommand::complete))));

        LogHelper.debug(KnowledgeCommand.class, "Registered QuestCommand");
    }

    private static int abandon(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        QuestData quests = getQuestData();

        String id = QuestIdArgType.getId(context, "id");
        Quest quest = quests.get(id).orElseThrow();
        quest.abandon(player);

        context.getSource().sendSuccess(new TranslatableComponent("commands.strange.abandoned_quest", id), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int complete(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        QuestData quests = getQuestData();

        String id = QuestIdArgType.getId(context, "id");
        Quest quest = quests.get(id).orElseThrow();
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

        Quest quest = new Quest(definition, 1.0F, null);
        quest.start(player);

        context.getSource().sendSuccess(new TranslatableComponent("commands.strange.started_quest", quest.getId()), false);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Unwraps the optional, throws an exception if savedData is not available.
     */
    private static QuestData getQuestData() throws CommandSyntaxException {
        Optional<QuestData> questSavedData = Quests.getQuestData();

        if (questSavedData.isEmpty()) {
            throw CommandHelper.makeException("Internal error", new TranslatableComponent("commands.strange.no_quest_data").getString());
        }

        return questSavedData.get();
    }

    private static CompletableFuture<Suggestions> getQuestIds(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<String> ids = new LinkedList<>();
        Quests.getQuestData().ifPresent(quests -> {
            List<Quest> all = quests.getAll();
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
