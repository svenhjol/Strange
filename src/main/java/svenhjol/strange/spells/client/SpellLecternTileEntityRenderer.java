package svenhjol.strange.spells.client;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.entity.model.BookModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import svenhjol.strange.Strange;
import svenhjol.strange.spells.block.SpellLecternBlock;
import svenhjol.strange.spells.tile.SpellLecternTileEntity;

import java.util.HashMap;
import java.util.Map;

public class SpellLecternTileEntityRenderer extends TileEntityRenderer<SpellLecternTileEntity>
{
    private static final Map<Integer, ResourceLocation> colormap = new HashMap<>();
    private final BookModel field_217656_d = new BookModel();

    public void render(SpellLecternTileEntity tile, double x, double y, double z, float partialTicks, int destroyStage)
    {
        BlockState state = tile.getBlockState();
        int color = state.get(SpellLecternBlock.COLOR);
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float)x + 0.5F, (float)y + 1.0F + 0.0625F, (float)z + 0.5F);
        float f = state.get(SpellLecternBlock.FACING).rotateY().getHorizontalAngle();
        GlStateManager.rotatef(-f, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(67.5F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translatef(0.0F, -0.125F, 0.0F);
        this.bindTexture(colormap.get(color));
        GlStateManager.enableCull();
        this.field_217656_d.render(0.0F, 0.1F, 0.9F, 1.2F, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
    }

    static
    {
        for (DyeColor value : DyeColor.values()) {
            colormap.put(value.getId(), new ResourceLocation(Strange.MOD_ID, "textures/entity/spell_book/spell_book_" + value.getName().toLowerCase() + ".png"));
        }
    }
}
