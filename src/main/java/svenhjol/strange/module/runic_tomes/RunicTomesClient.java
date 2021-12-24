package svenhjol.strange.module.runic_tomes;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.bookmarks.BookmarkBranch;
import svenhjol.strange.module.discoveries.DiscoveryBranch;
import svenhjol.strange.module.knowledge.branch.BiomeBranch;
import svenhjol.strange.module.knowledge.branch.DimensionBranch;
import svenhjol.strange.module.knowledge.branch.StructureBranch;
import svenhjol.strange.module.runic_tomes.network.ClientReceiveSetTome;

@ClientModule(module = RunicTomes.class)
public class RunicTomesClient extends CharmModule {
    public static ItemStack tomeHolder = null;

    public static ClientReceiveSetTome CLIENT_RECEIVE_SET_TOME;

    @Override
    public void register() {
        ScreenRegistry.register(RunicTomes.RUNIC_LECTERN_MENU, RunicLecternScreen::new);
        BlockEntityRendererRegistry.register(RunicTomes.RUNIC_LECTERN_BLOCK_ENTITY, RunicLecternRenderer::new);
        ItemProperties.register(RunicTomes.RUNIC_TOME, new ResourceLocation("branch"), this::handleItemPredicate);
    }

    @Override
    public void runWhenEnabled() {
        CLIENT_RECEIVE_SET_TOME = new ClientReceiveSetTome();
    }

    private float handleItemPredicate(ItemStack stack, ClientLevel level, LivingEntity entity, int i) {
        String tomeBranchName = RunicTomeItem.getBranch(stack);
        return switch (tomeBranchName) {
            case BiomeBranch.NAME -> 0.1F;
            case BookmarkBranch.NAME -> 0.2F;
            case DimensionBranch.NAME -> 0.3F;
            case DiscoveryBranch.NAME -> 0.4F;
            case StructureBranch.NAME -> 0.5F;
            default -> 0.0F;
        };
    }
}
