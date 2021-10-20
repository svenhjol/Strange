package svenhjol.strange.module.knowledge.branches;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.KnowledgeHelper;

import java.util.Optional;

public class DimensionsBranch extends KnowledgeBranch<Level, ResourceLocation> {
    @Override
    public String getBranchName() {
        return "Dimensions";
    }

    @Override
    public char getStartRune() {
        return KnowledgeHelper.getCharFromRange(Knowledge.JOURNEYMAN_RUNES, 1);
    }

    @Override
    public void register(Level type) {
        Optional.ofNullable(type.dimension().location()).ifPresent(res -> {
            String runes = getStartRune() + KnowledgeHelper.generateRunesFromResource(res, Knowledge.MAX_LENGTH);
            add(runes, res);
        });
    }

    public static DimensionsBranch load(CompoundTag tag) {
        DimensionsBranch branch = new DimensionsBranch();
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
        return get(runes).map(res -> StringHelper.snakeToPretty(res.getPath(), true));
    }
}
