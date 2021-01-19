package svenhjol.strange.base.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import svenhjol.charm.base.helper.PlayerHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.base.command.arg.QuestDefinitionArgType;
import svenhjol.strange.base.command.arg.QuestIdArgType;
import svenhjol.strange.base.command.arg.RuneArgType;
import svenhjol.strange.runestones.RunestonesHelper;
import svenhjol.strange.scrolls.ScrollDefinition;
import svenhjol.strange.scrolls.QuestManager;
import svenhjol.strange.scrolls.ScrollItem;
import svenhjol.strange.scrolls.Scrolls;
import svenhjol.strange.scrolls.populator.Populator;
import svenhjol.strange.scrolls.tag.Quest;

import java.util.List;
import java.util.Optional;

public class StrangeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal(Strange.MOD_ID)
            .then(CommandManager
                .literal("abandon_quest")
                .then(CommandManager.argument("id", QuestIdArgType.id())
                    .executes(StrangeCommand::abandonQuest)))
            .then(CommandManager
                .literal("abandon_my_quests")
                .executes(StrangeCommand::abandonMyQuests))
            .then(CommandManager
                .literal("abandon_all_quests")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(StrangeCommand::abandonAllQuests))
            .then(CommandManager
                .literal("claim_scroll")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(StrangeCommand::claimQuest))
            .then(CommandManager
                .literal("give_map")
                .then(CommandManager.argument("id", QuestIdArgType.id())
                    .executes(StrangeCommand::giveMap)))
            .then(CommandManager
                .literal("give_scroll")
                .then(CommandManager.argument("id", QuestIdArgType.id())
                    .executes(StrangeCommand::giveScroll)))
            .then(CommandManager
                .literal("learn_rune")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("letter", RuneArgType.letter())
                    .executes(StrangeCommand::learnRune)))
            .then(CommandManager
                .literal("learn_all_runes")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(StrangeCommand::learnAllRunes))
            .then(CommandManager
                .literal("list_my_quests")
                .executes(StrangeCommand::listMyQuests))
            .then(CommandManager
                .literal("list_all_quests")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(StrangeCommand::listAllQuests))
            .then(CommandManager
                .literal("start_quest")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("definition", QuestDefinitionArgType.definition())
                    .executes(StrangeCommand::startQuest)))
        );
    }

    private static int abandonQuest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        QuestManager questManager = getQuestManager();

        String id = QuestIdArgType.getId(context, "id");
        boolean result = questManager.abandonQuest(player, id);
        context.getSource().sendFeedback(new TranslatableText(result ? "scroll.strange.abandoned_quest" : "scroll.strange.no_quest_found", id), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int abandonMyQuests(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        QuestManager questManager = getQuestManager();
        questManager.abandonQuests(player);

        context.getSource().sendFeedback(new TranslatableText("scroll.strange.abandoned_quests"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int abandonAllQuests(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        QuestManager questManager = getQuestManager();
        questManager.abandonAllQuests();

        context.getSource().sendFeedback(new TranslatableText("scroll.strange.abandoned_all_quests"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int claimQuest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ItemStack held = player.getMainHandStack();
        if (!(held.getItem() instanceof ScrollItem))
            throw makeException("Invalid scroll", new TranslatableText("scroll.strange.claim_main_hand").getString());

        ScrollItem.claimOwnership(held, player);
        return Command.SINGLE_SUCCESS;
    }

    private static int giveMap(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        QuestManager questManager = getQuestManager();
        String questId = QuestIdArgType.getId(context, "id");
        Quest quest = getQuestById(questId);


        // check the player has an empty map and reduce by 1 when found
        consumeRequiredItem(player, new ItemStack(Items.MAP), "scroll.strange.map_required");


        // setup the quest populators and iterate through for maps
        List<Populator> populators = questManager.getPopulatorsForQuest(player, quest);

        populators.forEach(populator -> {
            ItemStack map = populator.getMap();
            if (map != ItemStack.EMPTY)
                PlayerHelper.addOrDropStack(player, map);
        });

        context.getSource().sendFeedback(new TranslatableText("scroll.strange.map_given", questId), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int giveScroll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String questId = QuestIdArgType.getId(context, "id");
        Quest quest = getQuestById(questId);

        // check the player has paper and reduce by 1 when found
        consumeRequiredItem(player, new ItemStack(Items.PAPER), "scroll.strange.paper_required");

        ItemStack scroll = new ItemStack(Scrolls.SCROLL_TIERS.get(quest.getTier()));
        ScrollItem.setScrollQuest(scroll, quest);
        ScrollItem.setScrollOwner(scroll, player);
        PlayerHelper.addOrDropStack(player, scroll);

        context.getSource().sendFeedback(new TranslatableText("scroll.strange.scroll_given", questId), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int learnRune(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Character rune = RuneArgType.getLetter(context, "letter");

        int runeVal = (int)rune - 97;
        if (runeVal < 0 || runeVal > RunestonesHelper.NUMBER_OF_RUNES - 1)
            throw makeException("Invalid rune value", "Must be lowercase letter from a to " + (char) (RunestonesHelper.NUMBER_OF_RUNES-1) + 97);

        ServerPlayerEntity player = context.getSource().getPlayer();
        RunestonesHelper.addLearnedRune(player, runeVal);

        context.getSource().sendFeedback(new TranslatableText("runestone.strange.learned_rune", rune), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int learnAllRunes(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        for (int i = 0; i < RunestonesHelper.NUMBER_OF_RUNES; i++) {
            RunestonesHelper.addLearnedRune(player, i);
        }

        context.getSource().sendFeedback(new TranslatableText("runestone.strange.learned_all_runes"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int listAllQuests(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        QuestManager questManager = getQuestManager();

        List<Quest> quests = questManager.getQuests();

        if (!quests.isEmpty()) {
            quests.forEach(quest
                -> context.getSource().sendFeedback(new LiteralText(quest.getTitle() + " > ").append(new LiteralText(quest.getId()).formatted(Formatting.AQUA)), false));
        } else {
            context.getSource().sendFeedback(new TranslatableText("scroll.strange.no_quests"), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int listMyQuests(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        QuestManager questManager = getQuestManager();

        List<Quest> quests = questManager.getQuests(player);

        if (!quests.isEmpty()) {
            quests.forEach(quest
                -> context.getSource().sendFeedback(new LiteralText(quest.getTitle() + " > ").append(new LiteralText(quest.getId()).formatted(Formatting.AQUA)), false));
        } else {
            context.getSource().sendFeedback(new TranslatableText("scroll.strange.no_quests"), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int startQuest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        QuestManager questManager = getQuestManager();

        int rarity = 1;
        ScrollDefinition definition = Scrolls.getDefinition(QuestDefinitionArgType.getDefinition(context, "definition"));
        if (definition == null)
            throw makeException("Invalid definition", new TranslatableText("scroll.strange.no_scroll_definition").getString());

        Quest quest = questManager.createQuest(player, definition, rarity, null);
        if (quest != null) {
            ScrollItem.giveScrollToPlayer(quest, player);
        } else {
            throw makeException("Quest failure", new TranslatableText("scroll.strange.create_quest_failed").getString());
        }

        context.getSource().sendFeedback(new TranslatableText("scroll.strange.started_quest", quest.getId()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static Quest getQuestById(String questId) throws CommandSyntaxException {
        Optional<Quest> optionalQuest = getQuestManager().getQuest(questId);
        if (!optionalQuest.isPresent())
            throw makeException("Invalid quest", new TranslatableText("scroll.strange.no_quest_found").getString());

        return optionalQuest.get();
    }

    private static void consumeRequiredItem(ServerPlayerEntity player, ItemStack requiredStack, String key) throws CommandSyntaxException {
        int slotWithStack = player.inventory.method_7371(requiredStack);
        if (!player.abilities.creativeMode) {
            if (slotWithStack < 0){
                throw makeException("Invalid item", new TranslatableText(key).getString());
            } else {
                player.inventory.getStack(slotWithStack).decrement(1);
            }
        }
    }

    public static CommandSyntaxException makeException(String type, String message, Object... args) {
        return new CommandSyntaxException(
            new SimpleCommandExceptionType(new LiteralText(type)),
            new LiteralText(String.format(message, args))
        );
    }

    /**
     * Unwraps the optional, throws an exception if QM is not available.
     * @return QuestManager
     * @throws CommandSyntaxException
     */
    public static QuestManager getQuestManager() throws CommandSyntaxException {
        Optional<QuestManager> questManager = Scrolls.getQuestManager();

        if (!questManager.isPresent())
            throw makeException("Internal error", new TranslatableText("scroll.strange.quest_manager_not_loaded").getString());

        return questManager.get();
    }
}
