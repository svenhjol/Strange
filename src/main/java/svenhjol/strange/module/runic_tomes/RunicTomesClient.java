package svenhjol.strange.module.runic_tomes;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.bookmarks.BookmarkBranch;
import svenhjol.strange.module.discoveries.DiscoveryBranch;
import svenhjol.strange.module.knowledge.branch.BiomeBranch;
import svenhjol.strange.module.knowledge.branch.DimensionBranch;
import svenhjol.strange.module.knowledge.branch.StructureBranch;

@ClientModule(module = RunicTomes.class)
public class RunicTomesClient extends CharmModule {
    public static ItemStack tomeHolder = null;

    @Override
    public void register() {
        ScreenRegistry.register(RunicTomes.RUNIC_LECTERN_MENU, RunicLecternScreen::new);
        BlockEntityRendererRegistry.register(RunicTomes.RUNIC_LECTERN_BLOCK_ENTITY, RunicLecternRenderer::new);
        ItemProperties.register(RunicTomes.RUNIC_TOME, new ResourceLocation("branch"), this::handleItemPredicate);
    }

    @Override
    public void runWhenEnabled() {
        ClientPlayNetworking.registerGlobalReceiver(RunicTomes.MSG_CLIENT_SET_LECTERN_TOME, this::handleSetTome);
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

    private void handleSetTome(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null) {
            LogHelper.error(this.getClass(), "Could not read tome tag from buffer");
            return;
        }

        client.execute(() -> {
            tomeHolder = ItemStack.of(tag);
            String runes = RunicTomeItem.getRunes(tomeHolder);
            if (runes.isEmpty()) {
                LogHelper.error(this.getClass(), "Could not fetch runes from tome");
                return;
            }
            LogHelper.debug(this.getClass(), "Tome set from server. Runes = " + runes);
        });
    }
}
