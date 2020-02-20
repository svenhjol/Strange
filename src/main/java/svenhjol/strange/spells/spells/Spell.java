package svenhjol.strange.spells.spells;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import svenhjol.meson.iface.IMesonEnum;
import svenhjol.strange.spells.module.Spells;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Spell
{
    public enum Affect implements IMesonEnum
    {
        NONE,
        TARGET,
        SELF,
        AREA,
        FOCUS
    }

    protected boolean needsActivation = false;
    protected int applyCost = 1;
    protected int uses = 1;
    protected String id;
    protected DyeColor color;
    protected Affect affect = Affect.NONE;

    public Spell(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public String getTranslationKey()
    {
        return "spell.strange." + this.id + ".title";
    }

    public String getDescriptionKey()
    {
        return "spell.strange." + this.id + ".desc";
    }

    public String getAffectKey()
    {
        return "spell.strange.affect." + getAffect().getName();
    }

    public DyeColor getColor()
    {
        return color;
    }

    public Affect getAffect()
    {
        return affect;
    }

    public int getApplyCost()
    {
        return applyCost;
    }

    public int getUses()
    {
        return uses;
    }

    public boolean needsActivation()
    {
        return this.needsActivation;
    }

    public CompoundNBT toNBT()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("id", this.id);
        return tag;
    }

    @Nullable
    public static Spell fromNBT(CompoundNBT tag)
    {
        String id = tag.getString("id");
        return Spells.spells.getOrDefault(id, null);
    }

    public abstract void cast(PlayerEntity player, ItemStack stone, Consumer<Boolean> didCast);

    protected void castArea(PlayerEntity player, int[] range, Consumer<List<BlockPos>> onEffect)
    {
        if (player.world.isRemote) return;
        ServerWorld world = (ServerWorld)player.world;
        BlockPos pos = player.getPosition();
        Stream<BlockPos> inRange = BlockPos.getAllInBox(pos.add(-range[0], -range[1], -range[2]), pos.add(range[0], range[1], range[2]));
        List<BlockPos> blocks = inRange.map(BlockPos::toImmutable).collect(Collectors.toList());

        onEffect.accept(blocks);

        double spread = 1.0D;
        for (BlockPos block : blocks) {
            if (world.rand.nextFloat() > 0.05F) continue;

            double px = block.getX() + 0.5D + (Math.random() - 0.5D) * spread;
            double py = block.getY() + 0.5D * spread;
            double pz = block.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
            world.spawnParticle(Spells.spellParticle, px, py, pz, 4, 0.0D, 0.0D, 0.0D, 3);
        }
    }
}