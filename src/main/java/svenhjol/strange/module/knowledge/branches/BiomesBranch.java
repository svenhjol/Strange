package svenhjol.strange.module.knowledge.branches;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.KnowledgeHelper;

import java.util.Optional;

public class BiomesBranch extends KnowledgeBranch<Biome, ResourceLocation> {
    public static final String NAME = "Biomes";

    @Override
    public String getBranchName() {
        return NAME;
    }

    @Override
    public char getStartRune() {
        return KnowledgeHelper.getCharFromRange(Knowledge.APPRENTICE_RUNES, 0);
    }

    @Override
    public void register(Biome type) {
        Optional.ofNullable(BuiltinRegistries.BIOME.getKey(type)).ifPresent(res -> {
            String runes = getStartRune() + KnowledgeHelper.generateRunesFromResource(res, Knowledge.MAX_LENGTH);
            add(runes, res);
        });
    }

    public static BiomesBranch load(CompoundTag tag) {
        BiomesBranch branch = new BiomesBranch();
        CompoundTag map = tag.getCompound(branch.getBranchName());
        map.getAllKeys().forEach(runes
            -> branch.add(runes, new ResourceLocation(map.getString(runes))));

        return branch;
    }

    @Override
    protected Tag tagify(ResourceLocation value) {
        return StringTag.valueOf(value.toString());
    }

    @Override
    public Optional<String> getPrettyName(String runes) {
        return get(runes).map(res -> StringHelper.snakeToPretty(res.getPath()));
    }
}
