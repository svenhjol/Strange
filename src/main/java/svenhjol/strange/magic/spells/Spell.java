package svenhjol.strange.magic.spells;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import svenhjol.strange.base.StrangeLoader;
import svenhjol.strange.magic.entity.TargettedSpellEntity;
import svenhjol.strange.magic.module.SpellBooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Spell
{
    public enum Effect
    {
        NONE,
        TARGET,
        SELF,
        AREA,
        FOCUS
    }

    protected int xpCost = 5;
    protected int duration = 60;

    public enum Element
    {
        NONE,
        LIFE,
        DEATH,
        AIR,
        WATER,
        EARTH,
        FIRE
    }

    protected String id;
    protected Element element = Element.NONE;
    protected Effect effect = Effect.NONE;

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
        return "spell.strange." + this.id;
    }

    public Element getElement()
    {
        return element;
    }

    public Effect getEffect()
    {
        return effect;
    }

    public int getDuration()
    {
        return duration;
    }

    public int getXpCost()
    {
        return xpCost;
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
        return SpellBooks.spells.getOrDefault(id, null);
    }

    /**
     * Called after the spell has been transferred to the staff.
     * {@link svenhjol.strange.magic.item.SpellBookItem#onItemUseFinish}
     * @param player
     * @param staff
     */
    public void transfer(PlayerEntity player, ItemStack staff)
    {
        // no op
    }

    /**
     * Called after the spell has been removed from the staff.
     * {@link svenhjol.strange.magic.item.StaffItem#cast}
     * @param player
     * @param staff
     * @return True if casting successful.
     */
    public abstract boolean cast(PlayerEntity player, ItemStack staff);

    protected void castArea(PlayerEntity player, int range, Consumer<List<BlockPos>> onEffect)
    {
        if (player.world.isRemote) return;
        ServerWorld world = (ServerWorld)player.world;
        BlockPos pos = player.getPosition();
        Stream<BlockPos> inRange = BlockPos.getAllInBox(pos.add(-range, -2, -range), pos.add(range, 2, range));
        List<BlockPos> blocks = inRange.map(BlockPos::toImmutable).collect(Collectors.toList());

        onEffect.accept(blocks);

        double spread = 1.0D;
        for (BlockPos block : blocks) {
            if (world.rand.nextFloat() > 0.3F) continue;

            for (int i = 0; i < 1; i++) {
                double px = block.getX() + 0.5D + (Math.random() - 0.5D) * spread;
                double py = block.getY() + 0.5D * spread;
                double pz = block.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
                world.spawnParticle(ParticleTypes.ENCHANT, px, py, pz, 5, 0.0D, 0.0D, 0.0D, 3);
            }
        }
    }

    protected void castTarget(PlayerEntity player, Consumer<RayTraceResult> onImpact)
    {
        World world = player.world;
        BlockPos playerPos = player.getPosition();

        float cx = -MathHelper.sin(player.rotationYaw * ((float)Math.PI / 180F)) * MathHelper.cos(player.rotationPitch * ((float)Math.PI / 180F));
        float cy = -MathHelper.sin(player.rotationPitch * ((float)Math.PI / 180F));
        float cz = MathHelper.cos(player.rotationYaw * ((float)Math.PI / 180F)) * MathHelper.cos(player.rotationPitch * ((float)Math.PI / 180F));
        Vec3d v1 = (new Vec3d(cx, cy, cz)).normalize();

        TargettedSpellEntity entity = new TargettedSpellEntity(world, v1.x, v1.y, v1.z);
        entity.setLocationAndAngles(playerPos.getX() + 0.5D, playerPos.getY() + 1.65D, playerPos.getZ() + 0.75D, 0.0F, 0.0F);
        entity.onImpact(onImpact);
        world.addEntity(entity);
    }

    protected void castFocus(PlayerEntity player, Consumer<BlockRayTraceResult> onFocusPos)
    {
        ServerWorld world = (ServerWorld)player.world;
        BlockRayTraceResult result = StrangeLoader.getBlockLookedAt(player);
        onFocusPos.accept(result);

        float spread = 0.5F;
        BlockPos focusPos = result.getPos().add(0, 1, 0);

        double px = focusPos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
        double py = focusPos.getY() + 1.5D * spread;
        double pz = focusPos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
        world.spawnParticle(ParticleTypes.ENCHANT, px, py, pz, 50, 0.0D, 0.0D, 0.0D, 1.5D);
    }
}