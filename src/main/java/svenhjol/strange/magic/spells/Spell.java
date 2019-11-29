package svenhjol.strange.magic.spells;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.meson.iface.IMesonEnum;
import svenhjol.strange.magic.entity.TargettedSpellEntity;
import svenhjol.strange.magic.module.Magic;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
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

    protected int castCost = 5;
    protected int applyCost = 1;
    protected int staffDamage = 1;
    protected int quantity = 32;
    protected float duration = 3;
    protected String id;
    protected Element element = Element.BASE;
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

    public Element getElement()
    {
        return element;
    }

    public Affect getAffect()
    {
        return affect;
    }

    public float getDuration()
    {
        return duration;
    }

    public int getCastCost()
    {
        return castCost;
    }

    public int getApplyCost()
    {
        return applyCost;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public int getStaffDamage()
    {
        return staffDamage;
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
        return Magic.spells.getOrDefault(id, null);
    }

    public boolean activate(PlayerEntity player, ItemStack book)
    {
        return true;
    }

    public abstract void cast(PlayerEntity player, ItemStack book, Consumer<Boolean> didCast);

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
            if (world.rand.nextFloat() > 0.18F) continue;

            for (int i = 0; i < 1; i++) {
                double px = block.getX() + 0.5D + (Math.random() - 0.5D) * spread;
                double py = block.getY() + 0.5D * spread;
                double pz = block.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
                world.spawnParticle(Magic.spellParticles.get(this.getElement()), px, py, pz, 5, 0.0D, 0.0D, 0.0D, 3);
            }
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

        TargettedSpellEntity entity = new TargettedSpellEntity(world, v1.x, v1.y, v1.z, this.element);
        entity.setLocationAndAngles(playerVec.x, playerVec.y + 1.65D, playerVec.z, 0.0F, 0.0F);
        entity.onImpact(onImpact);
        world.addEntity(entity);
    }

    protected void castFocus(PlayerEntity player, Consumer<BlockRayTraceResult> onFocusPos)
    {
        ServerWorld world = (ServerWorld)player.world;
        BlockRayTraceResult result = WorldHelper.getBlockLookedAt(player);
        onFocusPos.accept(result);

        float spread = 0.5F;
        BlockPos focusPos = result.getPos().add(0, 1, 0);

        double px = focusPos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
        double py = focusPos.getY() + 1.5D * spread;
        double pz = focusPos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
        world.spawnParticle(Magic.enchantParticles.get(this.getElement()), px, py, pz, 50, 0.0D, 0.0D, 0.0D, 1.5D);
    }

    public enum Element implements IMesonEnum
    {
        BASE(new float[] { 0.5F, 0.5F, 0.5F }, DyeColor.WHITE, TextFormatting.GRAY, "§7"),
        AIR(new float[] { 1.0F, 1.0F, 0.4F }, DyeColor.YELLOW, TextFormatting.YELLOW, "§e"),
        WATER(new float[] { 0.4F, 0.7F, 1.0F }, DyeColor.CYAN, TextFormatting.AQUA, "§b"),
        EARTH(new float[] { 0.4F, 1.0F, 0.5F }, DyeColor.LIME, TextFormatting.GREEN, "§a"),
        FIRE(new float[] { 1.0F, 0.2F, 0.0F }, DyeColor.RED, TextFormatting.RED, "§c");

        private final float[] color;
        private final String formatCode;
        private final TextFormatting formatColor;
        private final DyeColor dyeColor;

        Element(float[] color, DyeColor dyeColor, TextFormatting formatColor, String formatCode)
        {
            this.color = color;
            this.formatCode = formatCode;
            this.formatColor = formatColor;
            this.dyeColor = dyeColor;
        }

        public float[] getColor()
        {
            return this.color;
        }

        public String getFormatCode()
        {
            return formatCode;
        }

        public DyeColor getDyeColor()
        {
            return dyeColor;
        }

        public TextFormatting getFormatColor()
        {
            return formatColor;
        }
    }
}