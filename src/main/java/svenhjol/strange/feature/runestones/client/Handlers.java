package svenhjol.strange.feature.runestones.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import svenhjol.charm.charmony.feature.FeatureHolder;
import svenhjol.strange.api.impl.RunestoneLocation;
import svenhjol.strange.feature.runestones.RunestonesClient;
import svenhjol.strange.feature.runestones.common.Helpers;
import svenhjol.strange.feature.runestones.common.Networking;

import java.util.Map;
import java.util.WeakHashMap;

@SuppressWarnings("unused")
public final class Handlers extends FeatureHolder<RunestonesClient> {
    private long seed;
    private boolean hasReceivedSeed = false;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;

    public final Map<String, String> cachedRuneNames = new WeakHashMap<>();

    public Handlers(RunestonesClient feature) {
        super(feature);
    }

    public void hudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        feature().registers.hudRenderer.render(guiGraphics, deltaTracker);
    }

    public void playerTick(Player player) {
        if (player.level().isClientSide()) {
            feature().registers.hudRenderer.tick(player);
        }
    }

    public void worldSeedReceived(Player player, Networking.S2CWorldSeed packet) {
        this.seed = packet.seed();
        this.hasReceivedSeed = true;
        feature().handlers.cachedRuneNames.clear();
    }

    public void sacrificePositionReceived(Player player, Networking.S2CSacrificeInProgress packet) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var random = level.getRandom();
        var itemParticle = ParticleTypes.SMOKE;
        var runestoneParticle = ParticleTypes.ENCHANT;

        var itemDist = 0.1d;
        var itemPos = packet.itemPos();

        var runestoneDist = 2.4d;
        var runestonePos = packet.runestonePos();

        for (var i = 0; i < 8; i++) {
            level.addParticle(itemParticle, itemPos.x(), itemPos.y() + 0.36d, itemPos.z(),
                (itemDist / 2) - (random.nextDouble() * itemDist), 0, (itemDist / 2) - (random.nextDouble() * itemDist));
        }

        for (var i = 0; i < 8; i++) {
            level.addParticle(runestoneParticle, runestonePos.getX() + 0.5d, runestonePos.getY() + 0.60d, runestonePos.getZ() + 0.5d,
                (runestoneDist / 2) - (random.nextDouble() * runestoneDist), random.nextDouble(), (runestoneDist / 2) - (random.nextDouble() * runestoneDist));
        }
    }

    public void activateRunestoneReceived(Player player, Networking.S2CActivateRunestone packet) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var random = level.getRandom();
        var particle = ParticleTypes.LARGE_SMOKE;
        var pos = packet.pos();
        var range = 1.3d;

        for (var i = 0; i < 80; i++) {
            level.addParticle(particle,
                pos.getX() + 0.5d + ((random.nextDouble() * range) - (random.nextDouble() * range)),
                pos.getY() + 0.5d + ((random.nextDouble() * (range / 2)) - (random.nextDouble() * (range / 2))),
                pos.getZ() + 0.5d + ((random.nextDouble() * range) - (random.nextDouble() * range)), 0, 0, 0);
        }
    }

    public String runicName(RunestoneLocation location) {
        if (!hasReceivedSeed) {
            throw new RuntimeException("Client has not received the level seed");
        }

        var combined = location.type().name().substring(0, 2) + location.id().getPath();

        if (!cachedRuneNames.containsKey(combined)) {
            var random = RandomSource.create(seed);
            cachedRuneNames.put(combined, Helpers.generateRunes(combined, 16, random));
        }

        return cachedRuneNames.get(combined);
    }

    public BlockPos lookingAtBlock(Player player) {
        var cameraPosVec = player.getEyePosition(1.0f);
        var rotationVec = player.getViewVector(1.0f);
        var vec3d = cameraPosVec.add(rotationVec.x * 6, rotationVec.y * 6, rotationVec.z * 6);
        var raycast = player.level().clip(new ClipContext(cameraPosVec, vec3d, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        return raycast.getBlockPos();
    }

    public String localeKey(RunestoneLocation location) {
        var namespace = location.id().getNamespace();
        var path = location.id().getPath();

        return switch (location.type()) {
            case BIOME -> "biome." + namespace + "." + path;
            case STRUCTURE -> "structure." + namespace + "." + path;
            case PLAYER -> "player." + namespace + "." + path;
        };
    }

    public void renderScaledGuiItem(ItemStack stack, GuiGraphics guiGraphics, int x, int y, float scaleX, float scaleY) {
        var minecraft = Minecraft.getInstance();
        var itemRenderer = minecraft.getItemRenderer();
        var level = minecraft.level;

        this.scaleX = scaleX;
        this.scaleY = scaleY;
        guiGraphics.renderFakeItem(stack, x, y);
    }

    public void scaleItem(ItemStack stack, PoseStack poseStack) {
        poseStack.scale(scaleX, scaleY, 1.0f);
        scaleX = scaleY = 1.0f;
    }
}
