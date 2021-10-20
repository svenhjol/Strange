package svenhjol.strange.module.knowledge.branches;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.KnowledgeHelper;

import java.util.Optional;
import java.util.UUID;

public class PlayersBranch extends KnowledgeBranch<Player, UUID> {
    @Override
    public String getBranchName() {
        return "Players";
    }

    @Override
    public char getStartRune() {
        return KnowledgeHelper.getCharFromRange(Knowledge.JOURNEYMAN_RUNES, 0);
    }

    @Override
    public void register(Player type) {
        UUID uuid = type.getUUID();
        String runes = getStartRune() + KnowledgeHelper.generateRunesFromString(uuid.toString(), Knowledge.MAX_LENGTH);
        add(runes, uuid);
    }

    public static PlayersBranch load(CompoundTag tag) {
        PlayersBranch branch = new PlayersBranch();
        CompoundTag map = tag.getCompound(branch.getBranchName());
        map.getAllKeys().forEach(runes
            -> branch.add(runes, UUID.fromString(map.getString(runes))));

        return branch;
    }

    @Override
    public Tag tagify(UUID value) {
        return StringTag.valueOf(value.toString());
    }

    @Override
    public Optional<String> getPrettyName(String runes) {
        return get(runes).map(UUID::toString);
    }
}
