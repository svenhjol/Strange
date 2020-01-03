package svenhjol.strange.spells.spells;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.meson.iface.IMesonEnum;
import svenhjol.strange.spells.entity.TargettedSpellEntity;
import svenhjol.strange.spells.module.Spells;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
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

    public boolean activate(PlayerEntity player, ItemStack stone)
    {
        return true;
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

    protected void castTarget(PlayerEntity player, BiConsumer<RayTraceResult, TargettedSpellEntity> onImpact)
    {
        World world = player.world;
        Vec3d playerVec = player.getPositionVec();

        float cx = -MathHelper.sin(player.rotationYaw * ((float)Math.PI / 180F)) * MathHelper.cos(player.rotationPitch * ((float)Math.PI / 180F));
        float cy = -MathHelper.sin(player.rotationPitch * ((float)Math.PI / 180F));
        float cz = MathHelper.cos(player.rotationYaw * ((float)Math.PI / 180F)) * MathHelper.cos(player.rotationPitch * ((float)Math.PI / 180F));
        Vec3d v1 = (new Vec3d(cx, cy, cz)).normalize();

        TargettedSpellEntity entity = new TargettedSpellEntity(world, player, v1.x, v1.y, v1.z, this);
        entity.setLocationAndAngles(playerVec.x, playerVec.y + 1.5D, playerVec.z, 0.0F, 0.0F);
        entity.onImpact(onImpact);
        world.addEntity(entity);
    }

    protected void castFocus(PlayerEntity player, Consumer<BlockRayTraceResult> onFocusPos)
    {
        ServerWorld world = (ServerWorld)player.world;
        BlockRayTraceResult result = WorldHelper.getBlockLookedAt(player, 100);
        onFocusPos.accept(result);

        BlockPos focusPos = result.getPos().add(0, 1, 0);

        double px = focusPos.getX() + 0.5D;
        double py = focusPos.getY() + 1.5D;
        double pz = focusPos.getZ() + 0.5D;
        world.spawnParticle(Spells.enchantParticle, px, py, pz, 20, 0.1D, 0.1D, 0.1D, 1.2D);
    }

    protected void castSelf(PlayerEntity player, Consumer<ServerPlayerEntity> onSelf)
    {
        ServerWorld world = (ServerWorld)player.world;
        onSelf.accept((ServerPlayerEntity)player);

        Vec3d vec = player.getPositionVec();
        float spread = 0.5F;
        double px = vec.x + 0.5D + (Math.random() - 0.5D) * spread;
        double py = vec.y + 1.5D * spread;
        double pz = vec.z + 0.5D + (Math.random() - 0.5D) * spread;
        world.spawnParticle(Spells.spellParticle, px, py, pz, 50, 0.0D, 0.0D, 0.0D, 1.5D);
    }

    protected List<LivingEntity> getEntitiesAroundPos(World world, BlockPos pos, double[] range)
    {
        AxisAlignedBB bb = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        AxisAlignedBB area = bb.grow(range[0], range[1], range[2]);
        Predicate<LivingEntity> selector = Objects::nonNull;
        return world.getEntitiesWithinAABB(LivingEntity.class, area, selector);
    }

    @Nullable
    protected Entity getClosestEntity(World world, RayTraceResult result)
    {
        Entity e = null;
        if (result.getType() == RayTraceResult.Type.BLOCK) {
            List<LivingEntity> entities = getEntitiesAroundPos(world, ((BlockRayTraceResult) result).getPos(), new double[]{2, 2, 2});
            if (!entities.isEmpty()) e = entities.get(0);
        } else if (result.getType() == RayTraceResult.Type.ENTITY) {
            e = ((EntityRayTraceResult) result).getEntity();
        }

        return e;
    }
}