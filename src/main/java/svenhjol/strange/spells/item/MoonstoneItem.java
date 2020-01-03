package svenhjol.strange.spells.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.spells.helper.SpellsHelper;
import svenhjol.strange.spells.module.Moonstones;
import svenhjol.strange.spells.module.Spells;
import svenhjol.strange.spells.spells.Spell;
import vazkii.quark.api.IRuneColorProvider;
import vazkii.quark.api.QuarkCapabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MoonstoneItem extends MesonItem implements IRuneColorProvider
{
    public static final String SPELL = "spells";
    public static final String USES = "uses";
    public static final String META = "meta";

    public MoonstoneItem(MesonModule module)
    {
        super(module, "moonstone", new Item.Properties()
            .group(ItemGroup.TOOLS)
            .maxStackSize(1)
        );

        // allows different item icons to be shown. Each item icon has a float ref (see model)
        addPropertyOverride(new ResourceLocation("color"), (stone, world, entity) -> {
            Spell spell = getSpell(stone);
            float out = spell != null ? spell.getColor().getId() : 0;
            return out / 16.0F;
        });
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    {
        ItemStack stone = player.getHeldItem(hand);
        Spell spell = getSpell(stone);
        if (spell == null) return new ActionResult<>(ActionResultType.FAIL, stone);

        cast(player, stone);
        return new ActionResult<>(ActionResultType.SUCCESS, stone);
    }

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return Moonstones.glint && hasSpell(stack);
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }

    public static void cast(PlayerEntity player, ItemStack stone)
    {
        if (player.world.isRemote) return;

        Spell spell = getSpell(stone);
        if (spell == null) return;

        if (spell.needsActivation() && !hasMeta(stone)) {
            player.world.playSound(null, player.getPosition(), StrangeSounds.SPELL_CHARGE_SHORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            spell.activate(player, stone);
            return;
        }

        player.world.playSound(null, player.getPosition(), StrangeSounds.SPELL_CAST, SoundCategory.PLAYERS, 1.0F, 1.0F);
        player.getCooldownTracker().setCooldown(stone.getItem(), 20);

        spell.cast(player, stone, result -> {
            if (result) {
                Moonstones.effectEnchantStone((ServerPlayerEntity) player, spell, 5, 0.2D, 0.2D, 0.2, 2.2D);
                if (!player.isCreative()) {
                    boolean hasUses = decreaseUses(stone);
                    if (!hasUses) {
                        player.world.playSound(null, player.getPosition(), StrangeSounds.SPELL_NO_MORE_USES, SoundCategory.PLAYERS, 0.75F, hasUses ? 1.0F : 0.75F);
                    }
                }
            } else {
                player.world.playSound(null, player.getPosition(), StrangeSounds.SPELL_FAIL, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        });
    }

    public static void clear(ItemStack stone)
    {
        CompoundNBT tag = stone.getOrCreateTag();
        tag.remove(SPELL);
        tag.remove(META);
        tag.remove(USES);
    }

    public static boolean decreaseUses(ItemStack stone)
    {
        CompoundNBT tag = stone.getOrCreateTag();
        int remaining = tag.getInt(USES) - 1;

        if (remaining <= 0) {
            stone.shrink(1);
            return false;
        } else {
            tag.putInt(USES, remaining);
            return true;
        }
    }

    public static CompoundNBT getMeta(ItemStack stone)
    {
        return stone.getOrCreateChildTag(META);
    }

    @Nullable
    public static Spell getSpell(ItemStack stone)
    {
        String id = stone.getOrCreateTag().getString(SPELL);
        return Spells.spells.getOrDefault(id, null);
    }

    public static int getUses(ItemStack stone)
    {
        return stone.getOrCreateTag().getInt(USES);
    }

    public static boolean hasSpell(ItemStack stone)
    {
        return getSpell(stone) instanceof Spell;
    }

    public static boolean hasMeta(ItemStack stone)
    {
        CompoundNBT meta = getMeta(stone);
        boolean b = meta.isEmpty();
        return !b;
    }

    public static void putMeta(ItemStack stone, CompoundNBT tag)
    {
        CompoundNBT stoneTag = stone.getOrCreateTag();
        stoneTag.put(META, tag);
    }

    public static void putSpell(ItemStack stone, Spell spell)
    {
        CompoundNBT tag = stone.getOrCreateTag();
        tag.putString(SPELL, spell.getId());
        putUses(stone, spell.getUses());

        // change the stone name
        stone.setDisplayName(SpellsHelper.getSpellInfoText(spell));
    }

    public static void putUses(ItemStack stone, int uses)
    {
        stone.getOrCreateTag().putInt(USES, uses);
    }


//        addPropertyOverride(new ResourceLocation("aligned"), new IItemPropertyGetter()
//        {
//            @Override
//            public float call(ItemStack stack, @Nullable World world, @Nullable LivingEntity entityIn)
//            {
//                if (entityIn == null && !stack.isOnItemFrame()) {
//                    return 0;
//                }
//
//                boolean hasEntity = entityIn != null;
//                Entity entity = hasEntity ? entityIn : stack.getItemFrame();
//
//                if (world == null) {
//                    world = Objects.requireNonNull(entity).world;
//                }
//
//                BlockPos stonePos = getStonePos(stack);
//                if (stonePos == null) return 0;
//                if (entity == null) return 0;
//                BlockPos entityPos = entity.getPosition();
//
//                int stoneDim = getStoneDim(stack);
//                int entityDim = world.getDimension().getType().getId();
//
//                BlockPos adjusted = getAdjusted(stonePos, stoneDim, entityDim);
//
//                boolean alignedx = adjusted.getX() == entityPos.getX();
//                boolean alignedz = adjusted.getZ() == entityPos.getZ();
//
//                boolean origin = alignedx && alignedz;
//                boolean aligned = alignedx || alignedz;
//
//                if ((world.isRemote && aligned || origin) && entity instanceof PlayerEntity) {
//                    float pitch = 0.5F + (color.getId() / 16.0F);
//                    PlayerEntity player = (PlayerEntity)entity;
//
//                    if (aligned && !ItemNBTHelper.getBoolean(stack, svenhjol.charm.tools.item.MoonstoneItem.ALIGNED, false)) {
//                        world.playSound(player, entity.getPosition(), CharmSounds.HOMING, SoundCategory.BLOCKS, 0.55F, pitch);
//                    }
//                    if (origin && !ItemNBTHelper.getBoolean(stack, svenhjol.charm.tools.item.MoonstoneItem.ORIGIN, false)) {
//                        effectAtOrigin(world, stonePos);
//                        world.playSound(player, entity.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.BLOCKS, 1.0F, pitch);
//                        world.playSound(player, entity.getPosition(), CharmSounds.HOMING, SoundCategory.BLOCKS, 0.55F, pitch);
//                    }
//                }
//
//                ItemNBTHelper.setBoolean(stack, svenhjol.charm.tools.item.MoonstoneItem.ALIGNED, aligned);
//                ItemNBTHelper.setBoolean(stack, svenhjol.charm.tools.item.MoonstoneItem.ORIGIN, origin);
//
//                return 0;
//            }
//        });
//    }

//    private BlockPos getAdjusted(BlockPos stone, int stoneDim, int entityDim)
//    {
//        int stoneX = stone.getX();
//        int stoneZ = stone.getZ();
//
//        if (stoneDim == -1 && entityDim != -1) {
//            stoneX *= 8.0f;
//            stoneZ *= 8.0f;
//        } else if (entityDim == -1 && stoneDim != -1) {
//            stoneX /= 8.0f;
//            stoneZ /= 8.0f;
//        }
//
//        return new BlockPos(stoneX, 0, stoneZ);
//    }
//
//    @Override
//    public ActionResultType onItemUse(ItemUseContext context)
//    {
//        if (context.getPlayer() == null) return ActionResultType.FAIL;
//
//        PlayerEntity player = context.getPlayer();
//        World world = context.getWorld();
//        BlockPos pos = context.getPos();
//        Hand hand = context.getHand();
//        ItemStack item = player.getHeldItem(hand);
//        BlockPos stonePos = getStonePos(item);
//        int stoneDim = getStoneDim(item);
//
//        if (player.isSneaking() || stonePos == null) return ActionResultType.FAIL;
//
//        BlockPos adjusted = getAdjusted(stonePos, stoneDim, player.dimension.getId());
//
//        if (world.isRemote) {
//            int x = adjusted.getX() - pos.getX();
//            int z = adjusted.getZ() - pos.getZ();
//            int i = MathHelper.floor(MathHelper.sqrt((float) (x * x + z * z)));
//
//            String key;
//            if (i == 0) {
//                key = "gui.charm.moonstone_distance_0";
//                effectAtOrigin(world, adjusted.add(0, stonePos.getY(), 0));
//            } else {
//                key = i > 1 ? "gui.charm.moonstone_distance" : "gui.charm.moonstone_distance_1";
//            }
//            player.sendStatusMessage(new TranslationTextComponent(key, i), true);
//        }
//
//        return ActionResultType.PASS;
//    }

//    @Override
//    public boolean hasEffect(ItemStack stack)
//    {
//        return ItemNBTHelper.getBoolean(stack, ALIGNED, false) || ItemNBTHelper.getBoolean(stack, ORIGIN, false);
//    }

//    @Nullable
//    public static BlockPos getStonePos(ItemStack stack)
//    {
//        if (stack.hasTag()) {
//            int x = ItemNBTHelper.getInt(stack, X, 0);
//            int y = ItemNBTHelper.getInt(stack, Y, 0);
//            int z = ItemNBTHelper.getInt(stack, Z, 0);
//            return new BlockPos(x, y, z);
//        }
//        return null;
//    }
//
//    public static int getStoneDim(ItemStack stack)
//    {
//        if (stack.hasTag()) {
//            return ItemNBTHelper.getInt(stack, DIM, 0);
//        }
//        return 0;
//    }
//
//    public static ItemStack setStonePos(ItemStack stack, BlockPos pos)
//    {
//        ItemNBTHelper.setInt(stack, X, pos.getX());
//        ItemNBTHelper.setInt(stack, Y, pos.getY());
//        ItemNBTHelper.setInt(stack, Z, pos.getZ());
//        return stack;
//    }
//
//    public static ItemStack setStoneDim(ItemStack stack, int dim)
//    {
//        ItemNBTHelper.setInt(stack, DIM, dim);
//        return stack;
//    }

    @Override
    public void addInformation(ItemStack stone, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag)
    {
        super.addInformation(stone, world, tooltip, flag);
        Spell spell = getSpell(stone);
        if (spell == null) return;

        SpellsHelper.addSpellDescription(spell, tooltip);

        TranslationTextComponent usesText = new TranslationTextComponent("moonstone.strange.uses", getUses(stone));
        usesText.setStyle((new Style()).setColor(TextFormatting.YELLOW));
        tooltip.add(usesText);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getRuneColor(ItemStack stack)
    {
        Spell spell = getSpell(stack);
        if (spell == null) return 0;

        float[] components = spell.getColor().getColorComponentValues();
        return 0xFF000000 |
            ((int) (255 * components[0]) << 16) |
            ((int) (255 * components[1]) << 8) |
            (int) (255 * components[2]);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        final LazyOptional<IRuneColorProvider> holder = LazyOptional.of(() -> this);

        return new ICapabilityProvider() {

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                return QuarkCapabilities.RUNE_COLOR.orEmpty(cap, holder);
            }

        };
    }
}
