package svenhjol.strange.module;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.block.RunestoneBlock;
import svenhjol.strange.event.ThrownEntityImpactCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Module(description = "Runestones allow fast travel to points of interest in your world by using an Ender Pearl.")
public class Runestones extends MesonModule {
    public static final List<RunestoneBlock> runestones = new ArrayList<>();
    public static List<Destination> destinations = new ArrayList<>();

    private static final int numberOfRunes = 26;

    @Config(name = "Travel protection time", description = "Number of seconds of regeneration and slow-fall when travelling through a stone circle runestone.")
    public static int protectionDuration = 10;

    @Config(name = "Available destinations", description = "Destinations that runestones may teleport you to. The list is weighted with more likely runestones at the top.")
    public static List<String> availableDestinations = new ArrayList<>(Arrays.asList(
        "strange:spawn_point",
        "strange:stone_circle",
        "minecraft:village",
        "minecraft:pillager_outpost",
        "minecraft:desert_pyramid",
        "minecraft:jungle_pyramid",
        "minecraft:mineshaft",
        "minecraft:ocean_ruin",
        "minecraft:swamp_hut",
        "minecraft:igloo",
        "strange:underground_ruin"
    ));

    @Override
    public void register() {
        for (int i = 0; i < numberOfRunes; i++) {
            runestones.add(new RunestoneBlock(this, i));
        }
    }

    @Override
    public void init() {
        ThrownEntityImpactCallback.EVENT.register(this::tryEnderPearlImpact);
    }

    private ActionResult tryEnderPearlImpact(ThrownEntity entity, HitResult hitResult) {
        if (!entity.world.isClient
            && entity instanceof EnderPearlEntity
            && hitResult.getType() == HitResult.Type.BLOCK
            && entity.getOwner() instanceof PlayerEntity
        ) {
            PlayerEntity player = (PlayerEntity)entity.getOwner();
            if (player == null)
                return ActionResult.PASS;

            World world = entity.world;
            BlockPos pos = ((BlockHitResult)hitResult).getBlockPos();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block instanceof RunestoneBlock) {
                entity.remove();
                Meson.LOG.info("Hit a runestone block");
                world.playSound(null, pos, StrangeSounds.RUNESTONE_TRAVEL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    static class Destination {
        private Identifier structure;

        public Destination(Identifier structure) {
            this.structure = structure;
        }
    }
}
