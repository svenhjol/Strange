package svenhjol.strange.module.knowledge2.branch;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.module.knowledge2.exception.RegistrationException;
import svenhjol.strange.module.runes.RuneBranch;
import svenhjol.strange.module.runes.RuneHelper;
import svenhjol.strange.module.runes.Runes;
import svenhjol.strange.module.runes.Tier;

public class StructureBranch extends RuneBranch<StructureFeature<?>, ResourceLocation> {
    public static final String NAME = "Structures";

    @Override
    public ResourceLocation register(StructureFeature<?> structureFeature) {
        ResourceLocation id = Registry.STRUCTURE_FEATURE.getKey(structureFeature);

        if (id == null) {
            throw new RegistrationException("Could not register structure feature `" + structureFeature + "`");
        }

        String runes = getStartRune() + RuneHelper.getFromResource(id, Runes.MAX_PHRASE_LENGTH);
        add(runes, id);

        return id;
    }

    @Override
    public Tag getValueTag(ResourceLocation value) {
        return StringTag.valueOf(value.toString());
    }

    @Override
    public char getStartRune() {
        return RuneHelper.getFromRuneSet(Tier.APPRENTICE, 1);
    }

    @Override
    public @Nullable String getValueName(ResourceLocation value) {
        return StringHelper.snakeToPretty(value.getPath());
    }

    @Override
    public String getBranchName() {
        return NAME;
    }

    public static StructureBranch load(CompoundTag tag) {
        StructureBranch branch = new StructureBranch();
        CompoundTag map = tag.getCompound(branch.getBranchName());
        map.getAllKeys().forEach(runes -> branch.add(runes, new ResourceLocation(map.getString(runes))));
        return branch;
    }
}
