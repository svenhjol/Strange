package svenhjol.strange.module.knowledge2.branch;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.module.knowledge2.exception.RegistrationException;
import svenhjol.strange.module.runes.RuneBranch;
import svenhjol.strange.module.runes.RuneHelper;
import svenhjol.strange.module.runes.Runes;
import svenhjol.strange.module.runes.Tier;

public class BiomeBranch extends RuneBranch<Biome, ResourceLocation> {
    public static final String NAME = "Biomes";

    @Override
    public ResourceLocation register(Biome biome) {
        ResourceLocation id = BuiltinRegistries.BIOME.getKey(biome);

        if (id == null) {
            throw new RegistrationException("Could not register biome `" + biome + "`");
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
        return RuneHelper.getFromRuneSet(Tier.APPRENTICE, 0);
    }

    @Override
    public @Nullable String getValueName(String runes) {
        var biome = get(runes);
        return biome != null ? StringHelper.snakeToPretty(biome.getPath()) : null;
    }

    @Override
    public String getBranchName() {
        return NAME;
    }

    public static BiomeBranch load(CompoundTag tag) {
        BiomeBranch branch = new BiomeBranch();
        CompoundTag map = tag.getCompound(branch.getBranchName());
        map.getAllKeys().forEach(runes -> branch.add(runes, new ResourceLocation(map.getString(runes))));
        return branch;
    }
}
