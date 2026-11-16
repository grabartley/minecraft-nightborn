package com.grahambartley.nightborn.occult.goal;

import com.grahambartley.nightborn.occult.FamiliarMode;
import com.grahambartley.nightborn.occult.FamiliarRules;
import com.grahambartley.nightborn.occult.GhostlyFamiliarEntity;
import com.grahambartley.nightborn.occult.Servants;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

public class FamiliarScoutGoal extends Goal {

  static final int MARK_EVERY_TICKS = 10;
  static final double SPEED = 1.5;

  private final GhostlyFamiliarEntity familiar;
  private boolean returning;

  public FamiliarScoutGoal(final GhostlyFamiliarEntity familiar) {
    this.familiar = familiar;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  @Override
  public boolean canStart() {
    return familiar.mode() == FamiliarMode.SCOUT;
  }

  @Override
  public void start() {
    returning = false;
    familiar.getNavigation().stop();
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void tick() {
    final int ticks = familiar.scoutTick();
    final int limit = familiar.power().scoutTicks();
    final LivingEntity owner = familiar.ownerHere();
    final Vec3d target = familiar.scoutTarget();
    if (!returning
        && (target == null
            || FamiliarRules.scoutTurnsBack(ticks, limit, familiar.getPos().distanceTo(target)))) {
      returning = true;
    }
    if (returning
        && FamiliarRules.scoutHome(
            owner != null, owner == null ? 0.0 : familiar.distanceTo(owner), ticks, limit)) {
      familiar.order(FamiliarMode.FIGHT, owner);
      return;
    }
    final Vec3d heading =
        returning && owner != null ? owner.getPos().add(0.0, owner.getHeight() * 0.6, 0.0) : target;
    if (heading != null) {
      familiar.getMoveControl().moveTo(heading.x, heading.y, heading.z, SPEED);
      familiar.getLookControl().lookAt(heading.x, heading.y, heading.z);
    }
    if (ticks % MARK_EVERY_TICKS == 0) {
      mark(owner);
    }
  }

  private void mark(final LivingEntity owner) {
    final double radius = familiar.power().markRadius();
    for (final LivingEntity seen :
        familiar
            .getWorld()
            .getEntitiesByClass(
                LivingEntity.class,
                familiar.getBoundingBox().expand(radius),
                living ->
                    living != familiar
                        && living != owner
                        && living.isAlive()
                        && !Servants.sameMaster(living, familiar)
                        && FamiliarRules.marks(familiar.distanceTo(living), radius))) {
      seen.addStatusEffect(
          new StatusEffectInstance(
              StatusEffects.GLOWING, familiar.power().markTicks(), 0, false, false),
          familiar);
    }
  }
}
