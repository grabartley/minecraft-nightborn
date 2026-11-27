package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.mob.brain.Think;
import com.grahambartley.nightborn.skill.PowerShow;
import java.util.Optional;
import net.minecraft.entity.mob.MobEntity;

public final class MobFighting {

  public static final int NO_BLOOD_FLOOR = 0;

  public interface Moves {

    void tick();

    default void rest() {}
  }

  private final PowerCasting casting;
  private final Moves moves;

  public MobFighting(final PowerCasting casting, final Moves moves) {
    this.casting = casting;
    this.moves = moves;
  }

  public static boolean fights(
      final boolean hasTarget,
      final boolean downed,
      final boolean asleep,
      final boolean retreating,
      final boolean weakened) {
    return hasTarget && !downed && !asleep && !retreating && !weakened;
  }

  public <T extends MobEntity & PowerShow> void tick(
      final T fighter, final boolean fighting, final int thinkTicks, final int bloodFloor) {
    if (!fighting) {
      casting.cancel();
      moves.rest();
      return;
    }
    casting.tick(fighter, thinkTicks, bloodFloor);
    if (!casting.telling()) {
      moves.tick();
    }
  }

  public boolean telling() {
    return casting.telling();
  }

  public Optional<Think> lastThink() {
    return casting.lastThink();
  }
}
