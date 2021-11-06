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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.command.CommandHelper;
import svenhjol.strange.command.arg.RuneArgType;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.Journals;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeCommand {
    // from LocateBiomeCommand
    public static final DynamicCommandExceptionType ERROR_INVALID_BIOME;
    public static final DynamicCommandExceptionType ERROR_INVALID_STRUCTURE;

    public static final SuggestionProvider<CommandSourceStack> AVAILABLE_STRUCTURES;

    public static final Component LEARNED_ALL_STRUCTURES = new TranslatableComponent("commands.strange.learned_all_structures");
    public static final Component LEARNED_ALL_DIMENSIONS = new TranslatableComponent("commands.strange.learned_all_dimensions");
    public static final Component LEARNED_ALL_BIOMES = new TranslatableComponent("commands.strange.learned_all_biomes");
    public static final Component LEARNED_ALL_RUNES = new TranslatableComponent("commands.strange.learned_all_runes");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(Strange.MOD_ID)
            // strange learn_all_runes
            .then(Commands.literal("learn_all_biomes")
                .requires(source -> source.hasPermission(2))
                .executes(KnowledgeCommand::learnAllBiomes))

            // strange learn_all_runes
            .then(Commands.literal("learn_all_runes")
                .requires(source -> source.hasPermission(2))
                .executes(KnowledgeCommand::learnAllRunes))

            // strange learn_all_structures
            .then(Commands.literal("learn_all_structures")
                .requires(source -> source.hasPermission(2))
                .executes(KnowledgeCommand::learnAllStructures))

            // strange learn_all_dimensions
            .then(Commands.literal("learn_all_dimensions")
                .requires(source -> source.hasPermission(2))
                .executes(KnowledgeCommand::learnAllDimensions))

            // strange learn_biome
            .then(Commands.literal("learn_biome")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("biome", ResourceLocationArgument.id())
                    .suggests(SuggestionProviders.AVAILABLE_BIOMES)
                    .executes(context -> learnBiome(context.getSource(), context.getArgument("biome", ResourceLocation.class)))))

            // strange learn_structure
            .then(Commands.literal("learn_structure")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("structure", ResourceLocationArgument.id())
                    .suggests(AVAILABLE_STRUCTURES)
                    .executes(context -> learnStructure(context.getSource(), context.getArgument("structure", ResourceLocation.class)))))

            // strange learn_dimension
            .then(Commands.literal("learn_dimension")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("dimension", ResourceLocationArgument.id())
                    .suggests((context, builder) -> {
                        List<ResourceLocation> dimensions = new ArrayList<>();
                        context.getSource().getServer().getAllLevels().forEach(level -> dimensions.add(level.dimension().location()));
                        return SharedSuggestionProvider.suggestResource(dimensions, builder);
                    })
                    .executes(context -> learnDimension(context.getSource(), context.getArgument("dimension", ResourceLocation.class)))))

            // strange learn_rune
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

        knowledge.biomes.values().forEach(journal::learnBiome);

        context.getSource().sendSuccess(LEARNED_ALL_BIOMES, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int learnAllRunes(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        JournalData journal = getJournal(player);

        for (int i = 0; i < Knowledge.NUM_RUNES; i++) {
            journal.learnRune(i);
        }

        context.getSource().sendSuccess(LEARNED_ALL_RUNES, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int learnAllStructures(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        JournalData journal = getJournal(player);
        KnowledgeData knowledge = getKnowledge();

        knowledge.structures.values().forEach(journal::learnStructure);

        context.getSource().sendSuccess(LEARNED_ALL_STRUCTURES, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int learnAllDimensions(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        JournalData journal = getJournal(player);
        KnowledgeData knowledge = getKnowledge();

        knowledge.dimensions.values().forEach(journal::learnDimension);

        context.getSource().sendSuccess(LEARNED_ALL_DIMENSIONS, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int learnBiome(CommandSourceStack context, ResourceLocation res) throws CommandSyntaxException {
        Biome biome = context.getServer()
            .registryAccess()
            .registryOrThrow(Registry.BIOME_REGISTRY)
            .getOptional(res)
            .orElseThrow(() -> ERROR_INVALID_BIOME.create(res));

        if (biome == null) {
            throw CommandHelper.makeException("Invalid biome", "Something went wrong when trying to lookup the biome");
        }

        ServerPlayer player = context.getPlayerOrException();
        getJournal(player).learnBiome(res);

        context.sendSuccess(new TranslatableComponent("commands.strange.learned_biome", res), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int learnDimension(CommandSourceStack context, ResourceLocation res) throws CommandSyntaxException {
        MinecraftServer server = context.getServer();
        Iterable<ServerLevel> levels = server.getAllLevels();
        List<ResourceLocation> dimensions = new ArrayList<>();
        levels.forEach(level -> dimensions.add(level.dimension().location()));
        if (!dimensions.contains(res)) {
            throw CommandHelper.makeException("Invalid dimension", "Something went wrong when trying to lookup the dimension");
        }

        ServerPlayer player = context.getPlayerOrException();
        getJournal(player).learnDimension(res);

        context.sendSuccess(new TranslatableComponent("commands.strange.learned_dimension", res), false);
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

        if (structure == null) {
            throw CommandHelper.makeException("Invalid structure", "Something went wrong when trying to lookup the structure");
        }

        ServerPlayer player = context.getPlayerOrException();
        getJournal(player).learnStructure(res);

        context.sendSuccess(new TranslatableComponent("commands.strange.learned_structure", res), false);
        return Command.SINGLE_SUCCESS;
    }

    private static JournalData getJournal(ServerPlayer player) throws CommandSyntaxException {
        return Journals.getJournalData(player).orElseThrow(() -> CommandHelper.makeException("Journal error", "Could not load the player's journal"));
    }

    private static KnowledgeData getKnowledge() throws CommandSyntaxException {
        return Knowledge.getKnowledgeData().orElseThrow(() -> CommandHelper.makeException("Knowledge error", "Could not load knowledge data"));
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
