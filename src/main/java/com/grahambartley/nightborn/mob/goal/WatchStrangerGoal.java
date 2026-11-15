package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.home.NightRoutine;
import com.grahambartley.nightborn.mob.home.StrangerWatch;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public class WatchStrangerGoal extends Goal {

  static final int NOTICE_CHANCE = 40;
  static final int LONGEST_WATCH_TICKS = 600;
  static final int REPATH_TICKS = 10;
  static final double FOLLOW_SPEED = 0.9;

  private final VampireEntity vampire;
  @Nullable private PlayerEntity stranger;
  private int watched;
  private int repath;

  public WatchStrangerGoal(final VampireEntity vampire) {
    this.vampire = vampire;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (vampire.getRandom().nextInt(NOTICE_CHANCE) != 0 || !calm()) {
      return false;
    }
    stranger =
        vampire
            .getWorld()
            .getClosestPlayer(
                vampire.getX(),
                vampire.getY(),
                vampire.getZ(),
                StrangerWatch.NOTICE_RANGE,
                player ->
                    StrangerWatch.stranger(
                        true,
                        Vampires.isVampire((PlayerEntity) player),
                        player.isSpectator() || ((PlayerEntity) player).isCreative()));
    return stranger != null;
  }

  @Override
  public boolean shouldContinue() {
    return stranger != null
        && stranger.isAlive()
        && watched < LONGEST_WATCH_TICKS
        && calm()
        && StrangerWatch.keepsWatching(
            vampire.distanceTo(stranger),
            vampire.homeLife().withinRoam(vampire, NightRoutine.HOME_RADIUS));
  }

  @Override
  public void start() {
    watched = 0;
    repath = 0;
  }

  @Override
  public void tick() {
    if (stranger == null) {
      return;
    }
    watched++;
    vampire.getLookControl().lookAt(stranger, 30.0F, 30.0F);
    switch (StrangerWatch.move(vampire.distanceTo(stranger))) {
      case FOLLOW -> {
        if (--repath <= 0) {
          repath = REPATH_TICKS;
          vampire.getNavigation().startMovingTo(stranger, FOLLOW_SPEED);
        }
      }
      case WATCH -> vampire.getNavigation().stop();
      case WARN -> {
        vampire.getNavigation().stop();
        vampire.hiss();
      }
    }
  }

  @Override
  public void stop() {
    stranger = null;
    vampire.getNavigation().stop();
  }

  private boolean calm() {
    return vampire.getTarget() == null
        && !vampire.isSleeping()
        && !vampire.hungry()
        && Downed.state(vampire) == null;
  }
}
