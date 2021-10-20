package svenhjol.strange.module.knowledge.branches;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.KnowledgeHelper;

import java.util.Optional;

public class StructuresBranch extends KnowledgeBranch<StructureFeature<?>, ResourceLocation> {
    @Override
    public String getBranchName() {
        return "Structures";
    }

    @Override
    public char getStartRune() {
        return KnowledgeHelper.getCharFromRange(Knowledge.APPRENTICE_RUNES, 1);
    }

    @Override
    public void register(StructureFeature<?> type) {
        Optional.ofNullable(Registry.STRUCTURE_FEATURE.getKey(type)).ifPresent(res -> {
            String runes = getStartRune() + KnowledgeHelper.generateRunesFromResource(res, Knowledge.MAX_LENGTH);
            add(runes, res);
        });
    }

    public static StructuresBranch load(CompoundTag tag) {
        StructuresBranch branch = new StructuresBranch();
        CompoundTag map = tag.getCompound(branch.getBranchName());
        map.getAllKeys().forEach(runes
            -> branch.add(runes, new ResourceLocation(map.getString(runes))));

        return branch;
    }

    @Override
    public Tag tagify(ResourceLocation value) {
        return StringTag.valueOf(value.toString());
    }

    @Override
    public Optional<String> getPrettyName(String runes) {
        return get(runes).map(res -> StringHelper.snakeToPretty(res.getPath()));
    }
}
