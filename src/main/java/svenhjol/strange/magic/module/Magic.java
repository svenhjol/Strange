package svenhjol.strange.magic.module;

import com.google.common.base.CaseFormat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.magic.client.SpellsClient;
import svenhjol.strange.magic.entity.TargettedSpellEntity;
import svenhjol.strange.magic.helper.MagicHelper;
import svenhjol.strange.magic.item.SpellBookItem;
import svenhjol.strange.magic.item.StaffItem;
import svenhjol.strange.magic.spells.Spell;
import svenhjol.strange.magic.spells.Spell.Element;

import java.util.*;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.MAGIC, hasSubscriptions = true)
public class Magic extends MesonModule
{
    public static SpellBookItem book;
    public static EntityType<? extends Entity> entity;
    public static Map<String, Spell> spells = new HashMap<>();
    public static List<Item> staves = new ArrayList<>();
    public static List<String> validSpellNames = Arrays.asList(
        "blink",
        "explosion",
        "extinguish",
        "extraction",
        "growth",
        "knockback",
        "slowness",
        "thaw"
    );

    @OnlyIn(Dist.CLIENT)
    public static SpellsClient client;
    public static Map<Element, BasicParticleType> spellParticles = new HashMap<>();
    public static Map<Element, BasicParticleType> enchantParticles = new HashMap<>();

    @Override
    public void init()
    {
        // init items
        book = new SpellBookItem(this);

        staves.add(new StaffItem(this, "wooden", ItemTier.WOOD, 0.5F)
            .setDurationMultiplier(0.65F)
            .setAttackDamage(2.0F));

        staves.add(new StaffItem(this, "stone", ItemTier.STONE, 0.75F)
            .setCapacityMultiplier(1.25F)
            .setAttackDamage(3.0F));

        staves.add(new StaffItem(this, "iron", ItemTier.IRON, 1.0F)
            .setDurationMultiplier(0.85F)
            .setAttackDamage(4.0F));

        staves.add(new StaffItem(this, "golden", ItemTier.GOLD, 0.75F)
            .setDurationMultiplier(0.65F)
            .setCapacityMultiplier(1.25F)
            .setAttackDamage(2.0F));

        staves.add(new StaffItem(this, "diamond", ItemTier.DIAMOND, 0.75F)
            .setAttackDamage(5.0F));

        // add the valid spell instances
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

        // register targetted spell entity
        ResourceLocation res = new ResourceLocation(Strange.MOD_ID, "targetted_spell");
        entity = EntityType.Builder.create(TargettedSpellEntity::new, EntityClassification.MISC)
            .size(5.0F, 5.0F)
            .build(res.getPath())
            .setRegistryName(res);
        RegistryHandler.registerEntity(entity, res);

        // register spell and enchantment particles
        for (Element el : Element.values()) {
            ResourceLocation spellRes = new ResourceLocation(Strange.MOD_ID, el.getName() + "_spell");
            BasicParticleType spellType = new BasicParticleType(false);
            spellType.setRegistryName(spellRes);
            spellParticles.put(el, spellType);

            ResourceLocation enchantRes = new ResourceLocation(Strange.MOD_ID, el.getName() + "_enchant");
            BasicParticleType enchantType = new BasicParticleType(false);
            enchantType.setRegistryName(enchantRes);
            enchantParticles.put(el, enchantType);

            RegistryHandler.registerParticleType(spellType, spellRes);
            RegistryHandler.registerParticleType(enchantType, enchantRes);
        }
    }

    @Override
    public void setupClient(FMLClientSetupEvent event)
    {
        client = new SpellsClient();
    }

    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event)
    {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        ItemStack out;

        if (left.isEmpty() || right.isEmpty()) return;
        if (!(left.getItem() instanceof StaffItem)) return;
        if (right.getItem() != book) return;
        if (StaffItem.hasSpell(left)) return;

        Spell spell = SpellBookItem.getSpell(right);
        if (spell == null) return;

        int cost = spell.getApplyCost();

        out = left.copy();
        StaffItem.putSpell(out, spell);
        StaffItem.putOriginalName(out, left.getDisplayName());

        out.setDisplayName(new TranslationTextComponent("staff.strange.named_spell", left.getDisplayName().getFormattedText(), MagicHelper.getSpellInfoText(spell).getFormattedText()));

        event.setCost(cost);
        event.setMaterialCost(1);
        event.setOutput(out);
    }

    public static void effectEnchantStaff(ServerPlayerEntity player, Spell spell, int particles, double xOffset, double yOffset, double zOffset, double speed)
    {
        ServerWorld world = (ServerWorld) player.world;
        Vec3d playerVec = player.getPositionVec();

        for (int i = 0; i < 1; i++) {
            double px = playerVec.x;
            double py = playerVec.y + 1.75D;
            double pz = playerVec.z;
            world.spawnParticle(Magic.enchantParticles.get(spell.getElement()), px, py, pz, particles, xOffset, yOffset, zOffset, speed);
        }
    }
}
