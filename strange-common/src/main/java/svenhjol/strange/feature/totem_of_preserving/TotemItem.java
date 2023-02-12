package svenhjol.strange.feature.totem_of_preserving;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import svenhjol.charm.Charm;
import svenhjol.charm_core.base.CharmFeature;
import svenhjol.charm_core.base.CharmItem;
import svenhjol.charm_core.helper.ItemNbtHelper;
import svenhjol.charm_core.helper.TextHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TotemItem extends CharmItem {
    private static final String MESSAGE_TAG = "message";
    private static final String ITEMS_TAG = "items";

    public TotemItem(CharmFeature feature) {
        super(feature, new Item.Properties()
            .rarity(Rarity.UNCOMMON)
            .fireResistant()
            .stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack totem = player.getItemInHand(hand);

        // Don't break totem if it's empty.
        if (!TotemItem.hasItems(totem)) {
            return InteractionResultHolder.pass(totem);
        }

        var items = getItems(totem);

        if (!player.isSpectator() && !player.isCreative()) {
            totem.shrink(1);

            if (level.isClientSide()) {
                var pos = player.blockPosition();
                double spread = 1.5D;
                for (int i = 0; i < 4; i++) {
                    double px = pos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
                    double py = pos.getY() + 0.5D + (Math.random() - 0.5D) * spread;
                    double pz = pos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
                    level.addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, 0.0D, 0.1D, 0.0D);
                }
            } else {
                player.level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8F, 1.0F);
            }
        }

        if (!level.isClientSide()) {
            for (ItemStack stack : items) {
                var pos = player.blockPosition();
                var itemEntity = new ItemEntity(level, pos.getX(), pos.getY() + 0.5D, pos.getZ(), stack);
                level.addFreshEntity(itemEntity);
            }
        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag context) {
        var message = getMessage(stack);
        var items = getItems(stack);

        if (!message.isEmpty()) {
            tooltip.add(TextHelper.literal(message));
        }

        if (!items.isEmpty()) {
            var size = items.size();
            var str = size == 1 ? "totem.charm.preserving.item" : "totem.charm.preserving.items";
            tooltip.add(TextHelper.literal(I18n.get(str, size)));
        }

        super.appendHoverText(stack, level, tooltip, context);
    }

    public static String getMessage(ItemStack totem) {
        return ItemNbtHelper.getString(totem, MESSAGE_TAG, "");
    }

    public static List<ItemStack> getItems(ItemStack totem) {
        List<ItemStack> items = new ArrayList<>();
        var itemsTag = ItemNbtHelper.getCompound(totem, ITEMS_TAG);
        var keys = itemsTag.getAllKeys();

        for (var key : keys) {
            var tag = itemsTag.get(key);
            if (tag == null) {
                Charm.LOG.warn(TotemItem.class, "Missing item with key " + key);
                continue;
            }
            var stack = ItemStack.of((CompoundTag)tag);
            items.add(stack);
        }

        return items;
    }

    public static boolean hasItems(ItemStack totem) {
        return !getItems(totem).isEmpty();
    }

    public static void setMessage(ItemStack totem, String message) {
        ItemNbtHelper.setString(totem, MESSAGE_TAG, message);
    }

    public static void setItems(ItemStack totem, List<ItemStack> items) {
        var serialized = new CompoundTag();

        for (int i = 0; i < items.size(); i++) {
            var stack = items.get(i);
            serialized.put(Integer.toString(i), stack.save(new CompoundTag()));
        }

        ItemNbtHelper.setCompound(totem, ITEMS_TAG, serialized);
    }
}
