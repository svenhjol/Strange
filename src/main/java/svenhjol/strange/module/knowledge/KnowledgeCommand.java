package svenhjol.strange.module.knowledge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.command.CommandHelper;
import svenhjol.strange.command.arg.RuneArgType;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.JournalData;

public class KnowledgeCommand {
    // from LocateBiomeCommand
    public static final DynamicCommandExceptionType ERROR_INVALID_BIOME;
    public static final DynamicCommandExceptionType ERROR_INVALID_STRUCTURE;
    public static final SuggestionProvider<CommandSourceStack> AVAILABLE_STRUCTURES;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(Strange.MOD_ID + "_knowledge")
            // strange_knowledge learn_all_runes
            .then(Commands.literal("learn_all_biomes")
                .requires(source -> source.hasPermission(2))
                .executes(KnowledgeCommand::learnAllBiomes))

            // strange_knowledge learn_all_runes
            .then(Commands.literal("learn_all_runes")
                .requires(source -> source.hasPermission(2))
                .executes(KnowledgeCommand::learnAllRunes))

            // strange_knowledge learn_all_structures
            .then(Commands.literal("learn_all_structures")
                .requires(source -> source.hasPermission(2))
                .executes(KnowledgeCommand::learnAllStructures))

            // strange_knowledge learn_biome
            .then(Commands.literal("learn_biome")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("biome", ResourceLocationArgument.id())
                    .suggests(SuggestionProviders.AVAILABLE_BIOMES)
                    .executes(context -> learnBiome(context.getSource(), context.getArgument("biome", ResourceLocation.class)))))

            // strange_knowledge learn_structure
            .then(Commands.literal("learn_structure")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("structure", ResourceLocationArgument.id())
                    .suggests(AVAILABLE_STRUCTURES)
                    .executes(context -> learnStructure(context.getSource(), context.getArgument("structure", ResourceLocation.class)))))

            // strange_knowledge learn_rune
            .then(Commands.literal("learn_rune")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("letter", RuneArgType.letter())
                    .executes(KnowledgeCommand::learnRune)))
        );

        LogHelper.debug(KnowledgeCommand.class, "Registered KnowledgeCommand");
    }

    public static int learnAllBiomes(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        JournalData journal = getJournal(player);
        KnowledgeData knowledge = getKnowledge();

        knowledge.getBiomes().forEach((res, runes) -> journal.learnBiome(res));

        context.getSource().sendSuccess(new TranslatableComponent("commands.strange.learned_all_biomes"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int learnAllRunes(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        JournalData journal = getJournal(player);

        for (int i = 0; i < Knowledge.NUM_RUNES; i++) {
            journal.learnRune(i);
        }

        context.getSource().sendSuccess(new TranslatableComponent("commands.strange.learned_all_runes"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int learnAllStructures(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        JournalData journal = getJournal(player);
        KnowledgeData knowledge = getKnowledge();

        knowledge.getStructures().forEach((res, runes) -> journal.learnStructure(res));

        context.getSource().sendSuccess(new TranslatableComponent("commands.strange.learned_all_structures"), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int learnBiome(CommandSourceStack context, ResourceLocation res) throws CommandSyntaxException {
        Biome biome = context.getServer()
            .registryAccess()
            .registryOrThrow(Registry.BIOME_REGISTRY)
            .getOptional(res)
            .orElseThrow(() -> ERROR_INVALID_BIOME.create(res));

        if (biome == null)
            throw CommandHelper.makeException("Invalid biome", "Something went wrong when trying to lookup the biome");

        ServerPlayer player = context.getPlayerOrException();
        getJournal(player).learnBiome(res);

        context.sendSuccess(new TranslatableComponent("commands.strange.learned_biome", res), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int learnRune(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Character rune = RuneArgType.getLetter(context, "letter");

        int runeVal = (int)rune - 97;
        if (runeVal < 0 || runeVal >= Knowledge.NUM_RUNES)
            throw CommandHelper.makeException("Invalid rune value", "Must be lowercase letter from a to " + (char)(Knowledge.NUM_RUNES - 1) + 97);

        ServerPlayer player = context.getSource().getPlayerOrException();
        getJournal(player).learnRune(runeVal);

        context.getSource().sendSuccess(new TranslatableComponent("commands.strange.learned_rune", rune), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int learnStructure(CommandSourceStack context, ResourceLocation res) throws CommandSyntaxException {
        StructureFeature<?> structure = context.getServer()
            .registryAccess()
            .registryOrThrow(Registry.STRUCTURE_FEATURE_REGISTRY)
            .getOptional(res)
            .orElseThrow(() -> ERROR_INVALID_STRUCTURE.create(res));

        if (structure == null)
            throw CommandHelper.makeException("Invalid structure", "Something went wrong when trying to lookup the structure");

        ServerPlayer player = context.getPlayerOrException();
        getJournal(player).learnStructure(res);

        context.sendSuccess(new TranslatableComponent("commands.strange.learned_structure", res), false);
        return Command.SINGLE_SUCCESS;
    }

    private static JournalData getJournal(ServerPlayer player) throws CommandSyntaxException {
        return Journals.getPlayerData(player).orElseThrow(() -> CommandHelper.makeException("Journal error", "Could not load the player's journal"));
    }

    private static KnowledgeData getKnowledge() throws CommandSyntaxException {
        return Knowledge.getSavedData().orElseThrow(() -> CommandHelper.makeException("Knowledge error", "Could not load knowledge data"));
    }

    static {
        ERROR_INVALID_BIOME = new DynamicCommandExceptionType(object
            -> new TranslatableComponent("commands.strange.invalid_biome", object));

        ERROR_INVALID_STRUCTURE = new DynamicCommandExceptionType(object
            -> new TranslatableComponent("commands.strange.invalid_structure", object));

        AVAILABLE_STRUCTURES = SuggestionProviders.register(new ResourceLocation("available_structures"), (context, builder)
            -> SharedSuggestionProvider.suggestResource((context.getSource())
                .registryAccess()
                .registryOrThrow(Registry.STRUCTURE_FEATURE_REGISTRY).keySet(), builder));
    }
}
