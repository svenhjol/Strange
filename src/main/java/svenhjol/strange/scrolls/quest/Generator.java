package svenhjol.strange.scrolls.quest;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.meson.Meson;
import svenhjol.strange.outerlands.module.Outerlands;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.generator.*;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Generator
{
    public static Generator INSTANCE = new Generator();

    public IQuest generate(World world, BlockPos pos, Definition definition, float valueMultiplier, @Nullable IQuest quest)
    {
        if (quest == null) quest = new Quest();

        quest.generateId();
        quest.setTitle(definition.getTitle() + ".title");
        quest.setDescription(definition.getTitle() + ".desc");
        quest.setValue(Outerlands.getScaledMultiplier(world, pos) + valueMultiplier);

        // initialise generators
        List<Class<?>> generators = new ArrayList<>(Arrays.asList(
            LangGenerator.class,
            CraftGenerator.class,
            EncounterGenerator.class,
            GatherGenerator.class,
            HuntGenerator.class,
            LocateGenerator.class,
            MineGenerator.class,
            FetchGenerator.class,
            RewardGenerator.class,
            TimeGenerator.class
        ));

        for (Class<?> clazz : generators) {
            try {
                BaseGenerator gen = (BaseGenerator) clazz.getConstructor(World.class, BlockPos.class, IQuest.class, Definition.class).newInstance(world, pos, quest, definition);
                gen.generate();
            } catch (Exception e) {
                Meson.warn("Could not initialize generator " + clazz + ", skipping");
            }
        }

        return quest;
    }

    public IQuest generate(World world, BlockPos pos, float valueMultiplier, IQuest quest)
    {
        List<Definition> definitions = Quests.available.get(quest.getTier());
        Definition definition = definitions.get(world.rand.nextInt(definitions.size()));

        try {
            return generate(world, pos, definition, valueMultiplier, quest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
