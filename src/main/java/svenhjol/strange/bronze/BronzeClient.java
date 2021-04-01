package svenhjol.strange.bronze;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.mixin.object.builder.ModelPredicateProviderRegistryAccessor;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.entity.model.ShieldEntityModel;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.CharmTags;
import svenhjol.charm.event.ModelItemRenderCallback;
import svenhjol.charm.event.TextureStitchCallback;
import svenhjol.charm.mixin.accessor.BuiltInModelItemRendererAccessor;

import java.util.List;
import java.util.Set;

public class BronzeClient extends CharmClientModule {

    public static SpriteIdentifier SHIELD_BASE;
    public static SpriteIdentifier SHIELD_BASE_NO_PATTERN;

    private static final Identifier BASE_ID = new Identifier(Charm.MOD_ID, "entity/reinforced_shield_base");
    private static final Identifier BASE_NO_PATTERN_ID = new Identifier(Charm.MOD_ID, "entity/reinforced_shield_base_nopattern");

    public BronzeClient(CharmModule module) {
        super(module);
    }

    @Override
    public void register() {
        SHIELD_BASE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, BASE_ID);
        SHIELD_BASE_NO_PATTERN = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, BASE_NO_PATTERN_ID);

        ModelItemRenderCallback.EVENT.register(this::handleModelItemRender);
        TextureStitchCallback.EVENT.register(this::handleTextureStitch);

        ModelPredicateProviderRegistryAccessor.callRegister(new Identifier("blocking"), (itemStack, clientWorld, livingEntity, i)
            -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getActiveItem().isIn(CharmTags.SHIELDS) ? 1.0F : 0.0F);
    }

    private void handleTextureStitch(SpriteAtlasTexture atlas, Set<Identifier> textures) {
        textures.add(BASE_ID);
        textures.add(BASE_NO_PATTERN_ID);
    }

    private boolean handleModelItemRender(BuiltinModelItemRenderer renderer, MatrixStack matrices, ItemStack stack, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (stack.isOf(Bronze.REINFORCED_SHIELD)) {
            boolean bl = stack.getSubTag("BlockEntityTag") != null;
            matrices.push();
            matrices.scale(1.0F, -1.0F, -1.0F);
            SpriteIdentifier spriteIdentifier = bl ? SHIELD_BASE : SHIELD_BASE_NO_PATTERN;
            ShieldEntityModel modelShield = ((BuiltInModelItemRendererAccessor) renderer).getModelShield();
            VertexConsumer vertexConsumer = spriteIdentifier.getSprite().getTextureSpecificVertexConsumer(ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, modelShield.getLayer(spriteIdentifier.getAtlasId()), true, stack.hasGlint()));
            modelShield.getHandle().render(matrices, vertexConsumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            if (bl) {
                List<Pair<BannerPattern, DyeColor>> list = BannerBlockEntity.getPatternsFromNbt(ShieldItem.getColor(stack), BannerBlockEntity.getPatternListTag(stack));
                BannerBlockEntityRenderer.renderCanvas(matrices, vertexConsumers, light, overlay, modelShield.getPlate(), spriteIdentifier, false, list, stack.hasGlint());
            } else {
                modelShield.getPlate().render(matrices, vertexConsumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            }

            matrices.pop();
            return true;
        }

        return false;
    }
}
