package svenhjol.strange.spells.module;

import com.google.common.base.CaseFormat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.spells.client.SpellsClient;
import svenhjol.strange.spells.entity.TargettedSpellEntity;
import svenhjol.strange.spells.spells.Spell;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SPELLS, hasSubscriptions = true)
public class Spells extends MesonModule
{
    public static final String META = "meta";
    public static final String USES = "uses";

    public static EntityType<? extends Entity> entity;
    public static Map<String, Spell> spells = new HashMap<>();

    @Config(name = "Enabled spells", description = "List of all available spells.")
    public static List<String> enabledSpells = Arrays.asList(
        "aura",
        "blink",
        "boost",
        "drain",
        "explosion",
        "freeze",
        "growth",
        "heat",
        "knockback",
        "levitate",
        "lightning",
        "portal",
        "repel",
        "rise",
        "roots",
        "slowness",
        "summon",
        "transfer"
    );

    public static List<String> transferBlacklist = Arrays.asList(
        "minecraft:bedrock",
        "minecraft:end_portal_frame",
        "minecraft:end_portal",
        "minecraft:iron_door",
        "charm:rune_portal_frame",
        "charm:rune_portal"
    );

    public static List<String> transferHeavy = Arrays.asList(
        "minecraft:spawner",
        "minecraft:dragon_egg"
    );

    @OnlyIn(Dist.CLIENT)
    public static SpellsClient client;

    public static BasicParticleType spellParticle;
    public static BasicParticleType enchantParticle;

    @Override
    public void init()
    {
        // add the valid spell instances
        for (String id : enabledSpells) {
            String niceName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, id);
            String className = "svenhjol.strange.spells.spells." + niceName + "Spell";

            try {
                Meson.debug("Trying to load spell class " + className);
                Class<?> clazz = Class.forName(className);
                Spell instance = (Spell)clazz.getConstructor().newInstance();
                spells.put(id, instance);
            } catch (Exception e) {
                Meson.warn("Could not load spell " + id, e);
            }
        }

        // register targetted spell entity
        ResourceLocation res = new ResourceLocation(Strange.MOD_ID, "targetted_spell");
        entity = EntityType.Builder.create(TargettedSpellEntity::new, EntityClassification.MISC)
            .size(1.5F, 1.5F)
            .build(res.getPath())
            .setRegistryName(res);
        RegistryHandler.registerEntity(entity, res);

        ResourceLocation spellRes = new ResourceLocation(Strange.MOD_ID, "spell");
        BasicParticleType spellType = new BasicParticleType(false);
        spellType.setRegistryName(spellRes);
        spellParticle = spellType;

        ResourceLocation enchantRes = new ResourceLocation(Strange.MOD_ID, "enchant");
        BasicParticleType enchantType = new BasicParticleType(false);
        enchantType.setRegistryName(enchantRes);
        enchantParticle = enchantType;

        RegistryHandler.registerParticleType(spellType, spellRes);
        RegistryHandler.registerParticleType(enchantType, enchantRes);
    }

    @Override
    public void setupClient(FMLClientSetupEvent event)
    {
        client = new SpellsClient();
    }

    public static boolean activate(PlayerEntity player, ItemStack holdable, Spell spell)
    {
        if (spell.needsActivation() && !hasMeta(holdable)) {
            player.world.playSound(null, player.getPosition(), StrangeSounds.SPELL_CHARGE_SHORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            spell.activate(player, holdable);
            return true;
        }
        return false;
    }

    public static void cast(PlayerEntity player, ItemStack holdable, Spell spell, Consumer<ItemStack> onCastSuccess)
    {
        if (player.world.isRemote) return;
        if (spell == null) return;

        player.world.playSound(null, player.getPosition(), StrangeSounds.SPELL_CAST, SoundCategory.PLAYERS, 1.0F, 1.0F);
        player.getCooldownTracker().setCooldown(holdable.getItem(), 20);

        spell.cast(player, holdable, result -> {
            if (result) {
                clearMeta(holdable);
                effectEnchant((ServerWorld)player.world, player.getPositionVec(), spell, 5, 0.2D, 0.2D, 0.2, 2.2D);
                if (!player.isCreative()) {
                    boolean hasUses = decreaseUses(holdable);
                }
                onCastSuccess.accept(holdable);
            } else {
                player.world.playSound(null, player.getPosition(), StrangeSounds.SPELL_FAIL, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        });
    }

    public static boolean decreaseUses(ItemStack holdable)
    {
        CompoundNBT tag = holdable.getOrCreateTag();
        int remaining = tag.getInt(USES) - 1;

        if (remaining <= 0) {
            return false;
        } else {
            tag.putInt(USES, remaining);
            return true;
        }
    }

    public static void clearMeta(ItemStack holdable)
    {
        CompoundNBT holdableTag = holdable.getOrCreateTag();
        holdableTag.remove(META);
    }

    public static CompoundNBT getMeta(ItemStack holdable)
    {
        return holdable.getOrCreateChildTag(META);
    }

    public static void putMeta(ItemStack holdable, CompoundNBT tag)
    {
        CompoundNBT holdableTag = holdable.getOrCreateTag();
        holdableTag.put(META, tag);
    }

    public static boolean hasMeta(ItemStack holdable)
    {
        CompoundNBT meta = getMeta(holdable);
        boolean b = meta.isEmpty();
        return !b;
    }

    public static int getUses(ItemStack holdable)
    {
        return holdable.getOrCreateTag().getInt(USES);
    }

    public static void putUses(ItemStack holdable, int uses)
    {
        holdable.getOrCreateTag().putInt(USES, uses);
    }

    public static void effectEnchant(ServerWorld world, Vec3d vec, Spell spell, int particles, double xOffset, double yOffset, double zOffset, double speed)
    {
        for (int i = 0; i < 1; i++) {
            double px = vec.x;
            double py = vec.y + 1.75D;
            double pz = vec.z;
            world.spawnParticle(Spells.enchantParticle, px, py, pz, particles, xOffset, yOffset, zOffset, speed);
        }
    }
}
