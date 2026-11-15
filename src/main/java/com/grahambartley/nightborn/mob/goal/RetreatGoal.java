package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.config.CombatSettings;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.mob.combat.Retreat;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.EnumSet;
import java.util.function.IntSupplier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class RetreatGoal extends Goal {

  static final int SEARCH_HEIGHT = 6;

  private final PathAwareEntity mob;
  private final IntSupplier retreatHealth;
  @Nullable private LivingEntity threat;
  private int ticks;
  private long restUntil;

  public RetreatGoal(final PathAwareEntity mob, final IntSupplier retreatHealth) {
    this.mob = mob;
    this.retreatHealth = retreatHealth;
    setControls(EnumSet.of(Control.MOVE));
  }

  public boolean active() {
    return threat != null;
  }

  @Override
  public boolean canStart() {
    final LivingEntity target = mob.getTarget();
    if (target == null
        || Downed.state(mob) != null
        || !Retreat.starts(
            health(), retreatHealth.getAsInt(), mob.getWorld().getTime() < restUntil)) {
      return false;
    }
    return awayFrom(target) != null;
  }

  @Override
  public boolean shouldContinue() {
    return threat != null
        && Downed.state(mob) == null
        && !Retreat.ends(health(), retreatHealth.getAsInt(), ticks, settings());
  }

  @Override
  public void start() {
    ticks = 0;
    threat = mob.getTarget();
    move();
  }

  @Override
  public void tick() {
    ticks++;
    if (mob.getNavigation().isIdle()) {
      move();
    }
  }

  @Override
  public void stop() {
    threat = null;
    restUntil = mob.getWorld().getTime() + Retreat.restTicks(settings());
    mob.getNavigation().stop();
  }

  private void move() {
    if (threat != null) {
      final Vec3d away = awayFrom(threat);
      if (away != null) {
        mob.getNavigation().startMovingTo(away.x, away.y, away.z, Retreat.speed(settings()));
      }
    }
  }

  @Nullable
  private Vec3d awayFrom(final LivingEntity from) {
    return NoPenaltyTargeting.findFrom(
        mob, settings().retreatDistance(), SEARCH_HEIGHT, from.getPos());
  }

  private int health() {
    return Retreat.healthPercent(mob.getHealth(), mob.getMaxHealth());
  }

  private static CombatSettings settings() {
    return ServerConfigService.current().combat();
  }
}
