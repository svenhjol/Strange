package svenhjol.strange.module.casks;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.casks.network.ServerSendAddToCask;

import javax.annotation.Nullable;

@CommonModule(mod = Strange.MOD_ID, description = "Casks let you combine up to 64 potions, keeping an average of duration. Use glass bottles to extract home brew from the cask.")
public class Casks extends CharmModule {
    public static final ResourceLocation ID = new ResourceLocation(Strange.MOD_ID, "cask");
    public static final ResourceLocation TRIGGER_FILLED_WITH_POTION = new ResourceLocation(Strange.MOD_ID, "filled_with_potion");
    public static final ResourceLocation TRIGGER_TAKEN_BREW = new ResourceLocation(Strange.MOD_ID, "taken_brew");

    public static final String TAG_STORED_POTIONS = "StoredPotions";

    @Config(name = "Maximum bottles", description = "Maximum number of bottles a cask can hold.")
    public static int maxPortions = 16;

    @Config(name = "Preserve contents", description = "If true, a cask remembers it contents when broken.")
    public static boolean preserveContents = true;

    public static CaskBlock CASK;
    public static BlockEntityType<CaskBlockEntity> BLOCK_ENTITY;

    public static ServerSendAddToCask SERVER_SEND_ADD_TO_CASK;

    @Override
    public void register() {
        CASK = new CaskBlock(this);
        BLOCK_ENTITY = CommonRegistry.blockEntity(ID, CaskBlockEntity::new, CASK);
    }

    @Override
    public void runWhenEnabled() {
        PlayerBlockBreakEvents.BEFORE.register(this::handleBlockBreak);
        SERVER_SEND_ADD_TO_CASK = new ServerSendAddToCask();
    }

    public static void triggerFilledWithPotion(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_FILLED_WITH_POTION);
    }

    public static void triggerTakenBrew(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_TAKEN_BREW);
    }

    /**
     * Called just before cask is broken.  Fabric API event.
     */
    private boolean handleBlockBreak(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        if (!(state.getBlock() instanceof CaskBlock)) return true;

        if (blockEntity instanceof CaskBlockEntity cask) {
            ItemStack out = new ItemStack(CASK);

            if (preserveContents && cask.portions > 0) {
                CompoundTag tag = new CompoundTag();
                cask.saveAdditional(tag);
                out.getOrCreateTag().put(TAG_STORED_POTIONS, tag);
            }

            if (!cask.name.isEmpty()) {
                out.setHoverName(new TextComponent(cask.name));
            }

            level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), out));
        }

        return true;
    }
}
