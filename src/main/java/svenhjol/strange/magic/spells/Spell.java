package svenhjol.strange.magic.spells;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import svenhjol.meson.iface.IMesonEnum;
import svenhjol.strange.base.StrangeLoader;
import svenhjol.strange.magic.entity.TargettedSpellEntity;
import svenhjol.strange.magic.module.Spells;

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

    protected int xpCost = 5;
    protected int duration = 60;
    protected int staffDamage = 1;
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

    public int getDuration()
    {
        return duration;
    }

    public int getXpCost()
    {
        return xpCost;
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
        return Spells.spells.getOrDefault(id, null);
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
                world.spawnParticle(Spells.spellParticles.get(this.getElement()), px, py, pz, 5, 0.0D, 0.0D, 0.0D, 3);
            }
        }
    }

    protected void castTarget(PlayerEntity player, BiConsumer<RayTraceResult, TargettedSpellEntity> onImpact)
    {
        World world = player.world;
        BlockPos playerPos = player.getPosition();

        float cx = -MathHelper.sin(player.rotationYaw * ((float)Math.PI / 180F)) * MathHelper.cos(player.rotationPitch * ((float)Math.PI / 180F));
        float cy = -MathHelper.sin(player.rotationPitch * ((float)Math.PI / 180F));
        float cz = MathHelper.cos(player.rotationYaw * ((float)Math.PI / 180F)) * MathHelper.cos(player.rotationPitch * ((float)Math.PI / 180F));
        Vec3d v1 = (new Vec3d(cx, cy, cz)).normalize();

        TargettedSpellEntity entity = new TargettedSpellEntity(world, v1.x, v1.y, v1.z, this.element);
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
        world.spawnParticle(Spells.enchantParticles.get(this.getElement()), px, py, pz, 50, 0.0D, 0.0D, 0.0D, 1.5D);
    }

    public enum Element implements IMesonEnum
    {
        BASE(new float[] { 0.5F, 0.5F, 0.5F }, "§7", TextFormatting.GRAY),
        LIGHT(new float[] { 1.0F, 0.8F, 0.95F }, "§d", TextFormatting.LIGHT_PURPLE),
        DARK(new float[] { 0.75F, 0.0F, 1.0F }, "§5", TextFormatting.DARK_PURPLE),
        AIR(new float[] { 1.0F, 1.0F, 0.4F }, "§e", TextFormatting.YELLOW),
        WATER(new float[] { 0.4F, 0.7F, 1.0F }, "§b", TextFormatting.AQUA),
        EARTH(new float[] { 0.4F, 1.0F, 0.5F }, "§a", TextFormatting.GREEN),
        FIRE(new float[] { 1.0F, 0.2F, 0.0F }, "§c", TextFormatting.RED);

        private final float[] color;
        private final String formatCode;
        private final TextFormatting formatColor;

        Element(float[] color, String formatCode, TextFormatting formatColor)
        {
            this.color = color;
            this.formatCode = formatCode;
            this.formatColor = formatColor;
        }

        public float[] getColor()
        {
            return this.color;
        }

        public String getFormatCode()
        {
            return formatCode;
        }

        public TextFormatting getFormatColor()
        {
            return formatColor;
        }
    }
}