package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.mob.brain.FormHold;
import com.grahambartley.nightborn.mob.brain.Think;
import com.grahambartley.nightborn.skill.PowerShow;
import java.util.Optional;
import net.minecraft.entity.mob.MobEntity;

public final class MobFighting {

  public static final int NO_BLOOD_FLOOR = 0;

  public enum Engagement {
    FIGHTING,
    ESCAPING,
    STANDING_BY,
    NONE
  }

  public interface Moves {

    void tick();

    default void rest() {}
  }

  private final PowerCasting casting;
  private final Moves moves;
  private final boolean movesFirst;

  private MobFighting(final PowerCasting casting, final Moves moves, final boolean movesFirst) {
    this.casting = casting;
    this.moves = moves;
    this.movesFirst = movesFirst;
  }

  public static MobFighting castingFirst(final PowerCasting casting, final Moves moves) {
    return new MobFighting(casting, moves, false);
  }

  public static MobFighting movingFirst(final PowerCasting casting, final Moves moves) {
    return new MobFighting(casting, moves, true);
  }

  public static Engagement engagement(
      final boolean hasTarget,
      final boolean downed,
      final boolean asleep,
      final boolean escaping,
      final boolean weakened) {
    if (downed || asleep || weakened) {
      return Engagement.NONE;
    }
    if (escaping) {
      return Engagement.ESCAPING;
    }
    return hasTarget ? Engagement.FIGHTING : Engagement.STANDING_BY;
  }

  public <T extends MobEntity & PowerShow> void tick(
      final T fighter, final Engagement engagement, final int thinkTicks, final int bloodFloor) {
    if (engagement == Engagement.NONE) {
      casting.cancel();
      moves.rest();
      return;
    }
    if (engagement != Engagement.FIGHTING) {
      moves.rest();
      casting.tick(fighter, engagement == Engagement.ESCAPING, thinkTicks, bloodFloor);
      return;
    }
    final boolean hasTarget = fighter.getTarget() != null;
    if (movesFirst && !casting.telling()) {
      moves.tick();
    }
    casting.tick(fighter, hasTarget, thinkTicks, bloodFloor);
    if (!movesFirst && !casting.telling()) {
      moves.tick();
    }
  }

  public boolean telling() {
    return casting.telling();
  }

  public Optional<Think> lastThink() {
    return casting.lastThink();
  }

  public FormHold hold() {
    return casting.hold();
  }
}
