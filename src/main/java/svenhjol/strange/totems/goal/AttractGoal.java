package svenhjol.strange.totems.goal;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import java.util.EnumSet;

public class AttractGoal extends Goal
{
    private static final EntityPredicate ENTITY_PREDICATE = (new EntityPredicate()).setDistance(10.0D).allowInvulnerable().allowFriendlyFire().setSkipAttackChecks().setLineOfSiteRequired();
    protected final MobEntity creature;
    private final double speed;
    private double targetX;
    private double targetY;
    private double targetZ;
    private double pitch;
    private double yaw;
    protected PlayerEntity closestPlayer;
    private int delayTemptCounter;
    private final Ingredient temptItem;
    private final boolean scaredByPlayerMovement;

    public AttractGoal(MobEntity entity, double speed, Ingredient temptedBy, boolean scared) {
        this(entity, speed, scared, temptedBy);
    }

    public AttractGoal(MobEntity entity, double speed, boolean scared, Ingredient temptedBy) {
        this.creature = entity;
        this.speed = speed;
        this.temptItem = temptedBy;
        this.scaredByPlayerMovement = scared;
        this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
//        if (!(p_i47823_1_.getNavigator() instanceof GroundPathNavigator)) {
//            throw new IllegalArgumentException("Unsupported mob type for AttractGoal");
//        }
    }

    public boolean shouldExecute() {
        if (this.delayTemptCounter > 0) {
            --this.delayTemptCounter;
            return false;
        } else {
            this.closestPlayer = this.creature.world.getClosestPlayer(ENTITY_PREDICATE, this.creature);
            if (this.closestPlayer == null) {
                return false;
            } else {
                return this.isTempting(this.closestPlayer.getHeldItemMainhand()) || this.isTempting(this.closestPlayer.getHeldItemOffhand());
            }
        }
    }

    protected boolean isTempting(ItemStack p_188508_1_) {
        return this.temptItem.test(p_188508_1_);
    }

    public boolean shouldContinueExecuting() {
        if (this.isScaredByPlayerMovement()) {
            if (this.creature.getDistanceSq(this.closestPlayer) < 36.0D) {
                if (this.closestPlayer.getDistanceSq(this.targetX, this.targetY, this.targetZ) > 0.010000000000000002D) {
                    return false;
                }

                if (Math.abs((double)this.closestPlayer.rotationPitch - this.pitch) > 5.0D || Math.abs((double)this.closestPlayer.rotationYaw - this.yaw) > 5.0D) {
                    return false;
                }
            } else {
                this.targetX = this.closestPlayer.posX;
                this.targetY = this.closestPlayer.posY;
                this.targetZ = this.closestPlayer.posZ;
            }

            this.pitch = this.closestPlayer.rotationPitch;
            this.yaw = this.closestPlayer.rotationYaw;
        }

        return this.shouldExecute();
    }

    protected boolean isScaredByPlayerMovement() {
        return this.scaredByPlayerMovement;
    }

    public void startExecuting() {
        this.targetX = this.closestPlayer.posX;
        this.targetY = this.closestPlayer.posY;
        this.targetZ = this.closestPlayer.posZ;
    }

    public void resetTask() {
        this.closestPlayer = null;
        this.creature.getNavigator().clearPath();
        this.delayTemptCounter = 100;
    }

    public void tick() {
        this.creature.getLookController().setLookPositionWithEntity(this.closestPlayer, (float)(this.creature.getHorizontalFaceSpeed() + 20), (float)this.creature.getVerticalFaceSpeed());
        if (this.creature.getDistanceSq(this.closestPlayer) < 6.25D) {
            this.creature.getNavigator().clearPath();
        } else {
            this.creature.getNavigator().tryMoveToEntityLiving(this.closestPlayer, this.speed);
        }
    }
}
