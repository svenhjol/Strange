package svenhjol.strange.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.BiomeHelper;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.strange.module.RunicTablets;

import java.util.Optional;

public class BlankTabletItem extends TabletItem {
    public BlankTabletItem(CharmModule module, String name) {
        super(module, name, new Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack held = user.getStackInHand(hand);

        if (world.isClient)
            return TypedActionResult.fail(held);

        if (!user.isSneaking())
            return TypedActionResult.fail(held);

        if (!(held.getItem() instanceof BlankTabletItem))
            return TypedActionResult.fail(held);

        Identifier dimension = DimensionHelper.getDimension(world);
        BlockPos pos = user.getBlockPos();

        ItemStack tablet = new ItemStack(RunicTablets.RUNIC_TABLET);
        TabletItem.setDimension(tablet, dimension);
        TabletItem.setPos(tablet, pos);
        TabletItem.setExact(tablet, true);



        Optional<RegistryKey<Biome>> biomeKeyAtPosition = BiomeHelper.getBiomeKeyAtPosition((ServerWorld)world, pos);
        if (!biomeKeyAtPosition.isPresent()) {
            tablet.setCustomName(new TranslatableText("item.strange.runic_tablet"));
        } else {
            TranslatableText biomeName = new TranslatableText("biome.minecraft." + biomeKeyAtPosition.get().getValue().getPath());
            tablet.setCustomName(new TranslatableText("item.strange.runic_tablet_bound", biomeName));
        }

        user.setCurrentHand(hand);
        world.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
        return TypedActionResult.success(tablet, true);
    }
}
