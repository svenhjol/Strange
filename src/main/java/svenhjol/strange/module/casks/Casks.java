package svenhjol.strange.module.casks;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID, description = "Casks let you combine up to 64 potions, keeping an average of duration. Use glass bottles to extract home brew from the cask.")
public class Casks extends CharmModule {
    public static final ResourceLocation ID = new ResourceLocation(Strange.MOD_ID, "cask");
    public static final ResourceLocation TRIGGER_FILLED_WITH_POTION = new ResourceLocation(Strange.MOD_ID, "filled_with_potion");
    public static final ResourceLocation TRIGGER_TAKEN_BREW = new ResourceLocation(Strange.MOD_ID, "taken_brew");

    public static final String STORED_POTIONS_TAG = "StoredPotions";
    public static SoundEvent CASK_EXTRACT_SOUND;
    public static SoundEvent CASK_FILL_SOUND;
    public static SoundEvent CASK_INTERACT_SOUND;
    public static SoundEvent CASK_NAME_SOUND;

    @Config(name = "Maximum bottles", description = "Maximum number of bottles a cask can hold.")
    public static int maxPortions = 64;

    @Config(name = "Preserve contents", description = "If true, a cask remembers it contents when broken.")
    public static boolean preserveContents = true;

    public static CaskBlock CASK;
    public static BlockEntityType<CaskBlockEntity> BLOCK_ENTITY;
    public static final List<Item> VALID_INPUT_ITEMS = new ArrayList<>();

    public static ServerSendAddToCask SERVER_SEND_ADD_TO_CASK;

    @Override
    public void register() {
        CASK = new CaskBlock(this);
        BLOCK_ENTITY = CommonRegistry.blockEntity(ID, CaskBlockEntity::new, CASK);
        CASK_EXTRACT_SOUND = CommonRegistry.sound(new ResourceLocation(Strange.MOD_ID, "cask_extract"));
        CASK_FILL_SOUND = CommonRegistry.sound(new ResourceLocation(Strange.MOD_ID, "cask_fill"));
        CASK_INTERACT_SOUND = CommonRegistry.sound(new ResourceLocation(Strange.MOD_ID, "cask_interact"));
        CASK_NAME_SOUND = CommonRegistry.sound(new ResourceLocation(Strange.MOD_ID, "cask_name"));
    }

    @Override
    public void runWhenEnabled() {
        PlayerBlockBreakEvents.BEFORE.register(this::handleBlockBreak);
        SERVER_SEND_ADD_TO_CASK = new ServerSendAddToCask();
        VALID_INPUT_ITEMS.addAll(Arrays.asList(
            Items.GLASS_BOTTLE,
            Items.POTION
        ));
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
                out.getOrCreateTag().put(STORED_POTIONS_TAG, tag);
            }

            if (!cask.name.isEmpty()) {
                out.setHoverName(new TextComponent(cask.name));
            }

            level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), out));
        }

        return true;
    }
}
