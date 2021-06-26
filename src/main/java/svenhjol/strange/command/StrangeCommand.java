package svenhjol.strange.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.charm.helper.PlayerHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.command.arg.QuestDefinitionArgType;
import svenhjol.strange.command.arg.QuestIdArgType;
import svenhjol.strange.command.arg.RuneArgType;
import svenhjol.strange.module.runestones.RunestonesHelper;
import svenhjol.strange.module.scrolls.QuestSavedData;
import svenhjol.strange.module.scrolls.ScrollDefinition;
import svenhjol.strange.module.scrolls.ScrollItem;
import svenhjol.strange.module.scrolls.Scrolls;
import svenhjol.strange.module.scrolls.nbt.Quest;
import svenhjol.strange.module.scrolls.populator.BasePopulator;

import java.util.List;
import java.util.Optional;

public class StrangeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(Strange.MOD_ID)
            .then(Commands
                .literal("abandon_quest")
                .then(Commands.argument("id", QuestIdArgType.id())
                    .executes(StrangeCommand::abandonQuest)))
            .then(Commands
                .literal("abandon_my_quests")
                .executes(StrangeCommand::abandonMyQuests))
            .then(Commands
                .literal("abandon_all_quests")
                .requires(source -> source.hasPermission(2))
                .executes(StrangeCommand::abandonAllQuests))
            .then(Commands
                .literal("claim_scroll")
                .requires(source -> source.hasPermission(2))
                .executes(StrangeCommand::claimQuest))
            .then(Commands
                .literal("give_map")
                .then(Commands.argument("id", QuestIdArgType.id())
                    .executes(StrangeCommand::giveMap)))
            .then(Commands
                .literal("give_scroll")
                .then(Commands.argument("id", QuestIdArgType.id())
                    .executes(StrangeCommand::giveScroll)))
            .then(Commands
                .literal("learn_rune")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("letter", RuneArgType.letter())
                    .executes(StrangeCommand::learnRune)))
            .then(Commands
                .literal("learn_all_runes")
                .requires(source -> source.hasPermission(2))
                .executes(StrangeCommand::learnAllRunes))
            .then(Commands
                .literal("list_my_quests")
                .executes(StrangeCommand::listMyQuests))
            .then(Commands
                .literal("list_all_quests")
                .requires(source -> source.hasPermission(2))
                .executes(StrangeCommand::listAllQuests))
            .then(Commands
                .literal("start_quest")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("definition", QuestDefinitionArgType.definition())
                    .executes(StrangeCommand::startQuest)))
        );
    }

    private static int abandonQuest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        QuestSavedData savedData = getSavedData();

        String id = QuestIdArgType.getId(context, "id");
        boolean result = savedData.abandonQuest(player, id);
        context.getSource().sendSuccess(new TranslatableComponent(result ? "scroll.strange.abandoned_quest" : "scroll.strange.no_quest_found", id), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int abandonMyQuests(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        QuestSavedData savedData = getSavedData();
        savedData.abandonQuests(player);

        context.getSource().sendSuccess(new TranslatableComponent("scroll.strange.abandoned_quests"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int abandonAllQuests(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        QuestSavedData savedData = getSavedData();
        savedData.abandonAllQuests();

        context.getSource().sendSuccess(new TranslatableComponent("scroll.strange.abandoned_all_quests"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int claimQuest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof ScrollItem))
            throw makeException("Invalid scroll", new TranslatableComponent("scroll.strange.claim_main_hand").getString());

        ScrollItem.claimOwnership(held, player);
        return Command.SINGLE_SUCCESS;
    }

    private static int giveMap(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        QuestSavedData savedData = getSavedData();
        String questId = QuestIdArgType.getId(context, "id");
        Quest quest = getQuestById(questId);

        // check the player has an empty map and reduce by 1 when found
        consumeRequiredItem(player, new ItemStack(Items.MAP), "scroll.strange.map_required");

        // setup the quest populators and iterate through for maps
        List<BasePopulator> populators = savedData.getPopulatorsForQuest(player, quest);

        populators.forEach(populator -> {
            ItemStack map = populator.getMap();
            if (map != ItemStack.EMPTY)
                PlayerHelper.addOrDropStack(player, map);
        });

        context.getSource().sendSuccess(new TranslatableComponent("scroll.strange.map_given", questId), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int giveScroll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String questId = QuestIdArgType.getId(context, "id");
        Quest quest = getQuestById(questId);

        // check the player has paper and reduce by 1 when found
        consumeRequiredItem(player, new ItemStack(Items.PAPER), "scroll.strange.paper_required");

        ItemStack scroll = new ItemStack(Scrolls.SCROLL_TIERS.get(quest.getTier()));
        ScrollItem.setScrollQuest(scroll, quest);
        ScrollItem.setScrollOwner(scroll, player);
        PlayerHelper.addOrDropStack(player, scroll);

        context.getSource().sendSuccess(new TranslatableComponent("scroll.strange.scroll_given", questId), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int learnRune(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Character rune = RuneArgType.getLetter(context, "letter");

        int runeVal = (int)rune - 97;
        if (runeVal < 0 || runeVal > RunestonesHelper.NUMBER_OF_RUNES - 1)
            throw makeException("Invalid rune value", "Must be lowercase letter from a to " + (char) (RunestonesHelper.NUMBER_OF_RUNES-1) + 97);

        ServerPlayer player = context.getSource().getPlayerOrException();
        RunestonesHelper.addLearnedRune(player, runeVal);

        context.getSource().sendSuccess(new TranslatableComponent("runestone.strange.learned_rune", rune), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int learnAllRunes(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        for (int i = 0; i < RunestonesHelper.NUMBER_OF_RUNES; i++) {
            RunestonesHelper.addLearnedRune(player, i);
        }

        context.getSource().sendSuccess(new TranslatableComponent("runestone.strange.learned_all_runes"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int listAllQuests(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        QuestSavedData savedData = getSavedData();

        List<Quest> quests = savedData.getQuests();

        if (!quests.isEmpty()) {
            quests.forEach(quest
                -> context.getSource().sendSuccess(new TextComponent(quest.getTitle() + " > ").append(new TextComponent(quest.getId()).withStyle(ChatFormatting.AQUA)), false));
        } else {
            context.getSource().sendSuccess(new TranslatableComponent("scroll.strange.no_quests"), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int listMyQuests(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        QuestSavedData savedData = getSavedData();

        List<Quest> quests = savedData.getQuests(player);

        if (!quests.isEmpty()) {
            quests.forEach(quest
                -> context.getSource().sendSuccess(new TextComponent(quest.getTitle() + " > ").append(new TextComponent(quest.getId()).withStyle(ChatFormatting.AQUA)), false));
        } else {
            context.getSource().sendSuccess(new TranslatableComponent("scroll.strange.no_quests"), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int startQuest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        QuestSavedData savedData = getSavedData();

        int rarity = 1;
        ScrollDefinition definition = Scrolls.getDefinition(QuestDefinitionArgType.getDefinition(context, "definition"));
        if (definition == null)
            throw makeException("Invalid definition", new TranslatableComponent("scroll.strange.no_scroll_definition").getString());

        Quest quest = savedData.createQuest(player, definition, rarity, null);
        if (quest != null) {
            ScrollItem.giveScrollToPlayer(quest, player);
        } else {
            throw makeException("Quest failure", new TranslatableComponent("scroll.strange.create_quest_failed").getString());
        }

        context.getSource().sendSuccess(new TranslatableComponent("scroll.strange.started_quest", quest.getId()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static Quest getQuestById(String questId) throws CommandSyntaxException {
        Optional<Quest> optionalQuest = getSavedData().getQuest(questId);
        if (optionalQuest.isEmpty())
            throw makeException("Invalid quest", new TranslatableComponent("scroll.strange.no_quest_found").getString());

        return optionalQuest.get();
    }

    private static void consumeRequiredItem(ServerPlayer player, ItemStack requiredStack, String key) throws CommandSyntaxException {
        int slotWithStack = PlayerHelper.getInventory(player).findSlotMatchingItem(requiredStack);
        if (!PlayerHelper.getAbilities(player).instabuild) {
            if (slotWithStack < 0){
                throw makeException("Invalid item", new TranslatableComponent(key).getString());
            } else {
                PlayerHelper.getInventory(player).getItem(slotWithStack).shrink(1);
            }
        }
    }

    public static CommandSyntaxException makeException(String type, String message, Object... args) {
        return new CommandSyntaxException(
            new SimpleCommandExceptionType(new TextComponent(type)),
            new TextComponent(String.format(message, args))
        );
    }

    /**
     * Unwraps the optional, throws an exception if savedData is not available.
     */
    public static QuestSavedData getSavedData() throws CommandSyntaxException {
        Optional<QuestSavedData> questSavedData = Scrolls.getSavedData();

        if (questSavedData.isEmpty())
            throw makeException("Internal error", new TranslatableComponent("scroll.strange.saved_data_not_loaded").getString());

        return questSavedData.get();
    }
}
