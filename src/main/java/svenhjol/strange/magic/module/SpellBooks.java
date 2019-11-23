package svenhjol.strange.magic.module;

import com.google.common.base.CaseFormat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.magic.entity.TargettedSpellEntity;
import svenhjol.strange.magic.item.SpellBookItem;
import svenhjol.strange.magic.item.StaffItem;
import svenhjol.strange.magic.spells.Spell;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.MAGIC, hasSubscriptions = true)
public class SpellBooks extends MesonModule
{
    public static SpellBookItem item;

    public static EntityType<? extends Entity> entity;

    public static Map<String, Spell> spells = new HashMap<>();
    public static List<String> validSpellNames = Arrays.asList(
        "explosion",
        "extraction",
        "growth",
        "knockback",
        "magma_melting"
    );

    @Override
    public void init()
    {
        item = new SpellBookItem(this);

        for (String id : validSpellNames) {
            String niceName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, id);
            String className = "svenhjol.strange.magic.spells." + niceName + "Spell";

            try {
                Meson.debug("Trying to load spell class " + className);
                Class<?> clazz = Class.forName(className);
                Spell instance = (Spell)clazz.getConstructor().newInstance();
                spells.put(id, instance);
            } catch (Exception e) {
                Meson.warn("Could not load spell " + id, e);
            }
        }

        ResourceLocation res = new ResourceLocation(Strange.MOD_ID, "targetted_spell");

        entity = EntityType.Builder.create(TargettedSpellEntity::new, EntityClassification.MISC)
            .size(5.0F, 5.0F)
            .build(res.getPath())
            .setRegistryName(res);

        RegistryHandler.registerEntity(entity, res);
    }

    @SubscribeEvent
    public void onLeftClick(PlayerInteractEvent.LeftClickEmpty event)
    {
//        if (event.getPlayer() != null
//            && event.getItemStack().getItem() instanceof SpellBookItem
//        ) {
//            PlayerEntity player = event.getPlayer();
//            ItemStack book = event.getItemStack();
//            ItemStack staff = SpellBookItem.getStaffInOtherHand(player, book);
//            if (staff == null || !(staff.getItem() instanceof StaffItem)) return;
//
//            Hand staffHand = StaffItem.getHand(player, staff);
//            PacketHandler.sendToServer(new ServerStaffAction(ServerStaffAction.CAST, staffHand));
//        }
    }

    @SubscribeEvent
    public void onStartUsingItem(LivingEntityUseItemEvent.Start event)
    {
        if (event.getEntity() instanceof PlayerEntity
            && !event.getEntity().world.isRemote
            && event.getItem().getItem() instanceof SpellBookItem)
        {
            PlayerEntity player = (PlayerEntity)event.getEntity();
            World world = player.world;

            ItemStack book = event.getItem();
            ItemStack staff = SpellBookItem.getStaffInOtherHand(player, book);

            if (staff != null && staff.getItem() instanceof StaffItem) {
                StaffItem staffItem = (StaffItem) staff.getItem();
                Spell spell = SpellBookItem.getSpell(book);

                if (spell != null) {
                    if (spell.getXpCost() > player.experienceTotal) {
                        event.setCanceled(true);
                        return;
                    }

                    float duration = spell.getDuration() * staffItem.getTransferMultiplier();
                    event.setDuration((int) duration);

                    Meson.log("Start, setting duration to " + duration);
                    world.playSound(null, player.getPosition(), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 0.8F, 1.5F);

                }
            }
        }
    }

    @SubscribeEvent
    public void onUsingItem(LivingEntityUseItemEvent.Tick event)
    {
        if (event.getEntity() instanceof PlayerEntity
            && !event.getEntity().world.isRemote
            && event.getEntity().world.getGameTime() % 5 == 0
            && event.getItem().getItem() instanceof SpellBookItem)
        {
            Meson.log("Ticking");
            SpellBookItem.effectUseBook((ServerPlayerEntity)event.getEntity());
        }
    }
}
