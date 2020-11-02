package svenhjol.strange.runictablets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.StructureFeature;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.charm.base.helper.PosHelper;
import svenhjol.charm.base.item.CharmItem;
import svenhjol.strange.module.RunicTablets;
import svenhjol.strange.runestones.RunestoneHelper;

import javax.annotation.Nullable;
import java.util.Random;

public class RunicFragmentItem extends CharmItem {
    public static final String DIMENSION_TAG = "dimension";
    public static final String POS_TAG = "pos";
    public static final String FAILED_TAG = "failedDimensionCheck";

    public RunicFragmentItem(CharmModule module) {
        super(module, "runic_fragment", new Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack held = user.getStackInHand(hand);

        if (!world.isClient) {
            if (getPos(held) == null) {
                boolean result = populate(held, (ServerWorld) world, user.getBlockPos(), world.random);
                if (!result)
                    return TypedActionResult.fail(held);
            }

            user.sendMessage(new TranslatableText("runictablet.strange.fragment_symbol", held.getName()), true);
            user.getItemCooldownManager().set(this, 10);
        }

        return super.use(world, user, hand);
    }

    public static boolean populate(ItemStack fragment, ServerWorld world, BlockPos pos, Random random) {
        Identifier locationId = RunicTablets.destinations.get(random.nextInt(RunicTablets.destinations.size()));
        StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(locationId);

        if (structureFeature == null) {
            Charm.LOG.warn("Unable to find structure " + locationId + ", flagging fragment");
            RunicFragmentItem.setFailedDimensionCheck(fragment, DimensionHelper.getDimension(world));
            return false;
        }

        pos = PosHelper.addRandomOffset(pos, random, 6000);
        BlockPos foundPos = world.locateStructure(structureFeature, pos, 1000, false);

        if (foundPos == null) {
            Charm.LOG.warn("Unable to find position of structure in this dimension, flagging fragment");
            RunicFragmentItem.setFailedDimensionCheck(fragment, DimensionHelper.getDimension(world));
            return false;
        }

        setPos(fragment, foundPos);
        setDimension(fragment, DimensionHelper.getDimension(world));
        setFailedDimensionCheck(fragment, null);

        // TODO: this should probably be in some kind of helper
        Text label;
        if (locationId.getPath().equals("foundation")) {
            label = new TranslatableText("structure.strange.foundation");
        } else if (locationId.getPath().equals("ruin")) {
            label = new TranslatableText("structure.strange.ruin");
        } else {
            label = new LiteralText(RunestoneHelper.getFormattedLocationName(locationId));
        }
        fragment.setCustomName(label);

        return true;
    }

    public static boolean didFailDimensionCheck(ItemStack fragment, World world) {
        Identifier failedDimensionCheck = getFailedDimensionCheck(fragment);
        return failedDimensionCheck != null && DimensionHelper.isDimension(world, failedDimensionCheck);
    }

    @Nullable
    public static Identifier getDimension(ItemStack fragment) {
        if (fragment.getTag() == null || !fragment.getTag().contains(DIMENSION_TAG))
            return null;

        return new Identifier(fragment.getOrCreateTag().getString(DIMENSION_TAG));
    }

    @Nullable
    public static BlockPos getPos(ItemStack fragment) {
        if (fragment.getTag() == null || !fragment.getTag().contains(POS_TAG))
            return null;

        return BlockPos.fromLong(fragment.getOrCreateTag().getLong(POS_TAG));
    }

    @Nullable
    public static Identifier getFailedDimensionCheck(ItemStack fragment) {
        if (fragment.getTag() == null || !fragment.getTag().contains(FAILED_TAG))
            return null;

        return new Identifier(fragment.getOrCreateTag().getString(FAILED_TAG));
    }

    public static void setPos(ItemStack fragment, BlockPos pos) {
        fragment.getOrCreateTag().putLong(POS_TAG, pos.asLong());
    }

    public static void setDimension(ItemStack fragment, Identifier dimension) {
        fragment.getOrCreateTag().putString(DIMENSION_TAG, dimension.toString());
    }

    public static void setFailedDimensionCheck(ItemStack fragment, @Nullable Identifier dimension) {
        if (dimension == null) {
            fragment.getOrCreateTag().remove(FAILED_TAG);
        } else {
            fragment.getOrCreateTag().putString(FAILED_TAG, dimension.toString());
        }
    }
}
