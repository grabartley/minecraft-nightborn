package com.grahambartley.nightborn.wild;

import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public final class WatchFromAfarGoal extends Goal {

  static final int STEP = 16;
  static final int HEIGHT = 7;
  static final float TURN_SPEED = 30.0F;

  private final VampireEntity vampire;

  public WatchFromAfarGoal(final VampireEntity vampire) {
    this.vampire = vampire;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  @Override
  public boolean canStart() {
    return watched().isPresent();
  }

  @Override
  public boolean shouldContinue() {
    return canStart();
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void tick() {
    watched()
        .ifPresent(
            player -> {
              vampire.getLookControl().lookAt(player, TURN_SPEED, TURN_SPEED);
              final int keep = ServerConfigService.current().firstNights().watcherDistance();
              if (!WildRules.backsOff(vampire.squaredDistanceTo(player), keep)) {
                vampire.getNavigation().stop();
              } else if (vampire.getNavigation().isIdle()) {
                final Vec3d away =
                    NoPenaltyTargeting.findFrom(vampire, STEP, HEIGHT, player.getPos());
                if (away != null) {
                  vampire.getNavigation().startMovingTo(away.x, away.y, away.z, 1.0);
                }
              }
            });
  }

  private Optional<PlayerEntity> watched() {
    return WandererAttachment.get(vampire)
        .flatMap(Wanderer::watches)
        .map(vampire.getWorld()::getPlayerByUuid);
  }
}
