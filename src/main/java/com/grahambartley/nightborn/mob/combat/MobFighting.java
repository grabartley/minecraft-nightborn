package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.mob.brain.ActionReadiness;
import com.grahambartley.nightborn.mob.brain.FormHold;
import com.grahambartley.nightborn.mob.brain.Think;
import com.grahambartley.nightborn.skill.PowerShow;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

public final class MobFighting {

  public static final int NO_BLOOD_FLOOR = 0;

  public enum Engagement {
    FIGHTING,
    ESCAPING,
    STANDING_BY,
    NONE
  }

  public interface Moves {

    Moves NONE = () -> {};

    void tick();

    default void rest() {}

    default List<ActionReadiness> actions() {
      return List.of();
    }

    default boolean act(final Identifier action) {
      return false;
    }

    default boolean reacting() {
      return false;
    }

    default void reacted() {}
  }

  private final PowerCasting casting;
  private final Moves moves;

  public MobFighting(final PowerCasting casting, final Moves moves) {
    this.casting = casting;
    this.moves = moves;
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
      casting.tick(fighter, engagement == Engagement.ESCAPING, thinkTicks, bloodFloor, Moves.NONE);
      return;
    }
    if (!casting.telling()) {
      moves.tick();
      if (moves.reacting() && casting.react(fighter, bloodFloor)) {
        moves.reacted();
        return;
      }
    }
    casting.tick(fighter, fighter.getTarget() != null, thinkTicks, bloodFloor, moves);
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
