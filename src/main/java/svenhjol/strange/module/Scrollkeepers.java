package svenhjol.strange.module;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestType;
import svenhjol.meson.MesonModule;
import svenhjol.meson.event.PlayerTickCallback;
import svenhjol.meson.helper.VillagerHelper;
import svenhjol.meson.iface.Module;
import svenhjol.meson.mixin.accessor.RenderLayersAccessor;
import svenhjol.strange.Strange;
import svenhjol.strange.block.WritingDeskBlock;
import svenhjol.strange.client.ScrollKeepersClient;
import svenhjol.strange.item.ScrollItem;
import svenhjol.strange.mixin.accessor.VillagerEntityAccessor;
import svenhjol.strange.scroll.tag.QuestTag;

@Module(description = "Scrollkeepers are villagers that sell scrolls and accept completed quests.")
public class Scrollkeepers extends MesonModule {
    public static Identifier BLOCK_ID = new Identifier(Strange.MOD_ID, "writing_desk");
    public static Identifier VILLAGER_ID = new Identifier(Strange.MOD_ID, "scrollkeeper");

    public static WritingDeskBlock WRITING_DESK;
    public static VillagerProfession SCROLLKEEPER;
    public static PointOfInterestType POIT;

    public static ScrollKeepersClient client;

    public static int interestRange = 16;

    @Override
    public void register() {
        // TODO: dedicated sounds for scrollkeeper jobsite
        WRITING_DESK = new WritingDeskBlock(this);
        POIT = VillagerHelper.addPointOfInterestType(BLOCK_ID, WRITING_DESK, 1);
        SCROLLKEEPER = VillagerHelper.addProfession(VILLAGER_ID, POIT, SoundEvents.ENTITY_VILLAGER_WORK_LIBRARIAN);

        // TODO: village builds for scrollkeepers
    }

    @Override
    public void clientRegister() {
        RenderLayersAccessor.getBlocks().put(WRITING_DESK, RenderLayer.getCutout());
    }

    @Override
    public void init() {
        UseEntityCallback.EVENT.register(this::tryHandInScroll);
    }

    @Override
    public void clientInit() {
        client = new ScrollKeepersClient(this);
        PlayerTickCallback.EVENT.register(player -> client.villagerInterested(player));
    }

    private ActionResult tryHandInScroll(PlayerEntity playerEntity, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        if (entity instanceof VillagerEntity) {
            ItemStack heldStack = playerEntity.getStackInHand(hand);
            VillagerEntity villager = (VillagerEntity)entity;
            if (!(heldStack.getItem() instanceof ScrollItem))
                return ActionResult.PASS;

            if (!world.isClient) {
                QuestTag quest = ScrollItem.getScrollQuest(heldStack);
                if (quest == null)
                    return ActionResult.PASS;

                if (!quest.isSatisfied(playerEntity)) {
                    ((VillagerEntityAccessor)villager).invokeSayNo();
                    return ActionResult.FAIL;
                }

                world.playSound(null, playerEntity.getBlockPos(), SoundEvents.ENTITY_VILLAGER_YES, SoundCategory.PLAYERS, 1.0F, 1.0F);
                world.playSound(null, playerEntity.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0F, 1.0F);

                quest.complete(playerEntity);
                heldStack.decrement(1);
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
