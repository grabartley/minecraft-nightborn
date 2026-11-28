package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.mob.brain.ActionReadiness;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Identifier;

public final class HunterSupport {

  public static final Identifier HOLY_WATER = Identifier.of(Nightborn.MOD_ID, "holy_water");
  public static final Identifier CRUCIFIX = Identifier.of(Nightborn.MOD_ID, "crucifix");

  public record Moment(
      boolean foe,
      double distance,
      boolean busy,
      boolean throwing,
      boolean canThrow,
      boolean raised) {}

  private HunterSupport() {}

  public static List<ActionReadiness> actions(
      final boolean holyWater,
      final boolean crucifix,
      final Moment moment,
      final double wardRange) {
    final List<ActionReadiness> actions = new ArrayList<>(2);
    if (holyWater) {
      actions.add(
          ActionReadiness.of(
              HOLY_WATER,
              moment.foe() && !moment.busy() && moment.canThrow(),
              HunterActionTimer.THROW_COOLDOWN_TICKS));
    }
    if (crucifix) {
      actions.add(
          ActionReadiness.of(
              CRUCIFIX,
              moment.foe() && !moment.throwing() && moment.distance() <= wardRange,
              0,
              moment.raised()));
    }
    return actions;
  }

  public static boolean holds(
      final boolean hunting,
      final boolean foe,
      final boolean sees,
      final double distance,
      final double wardRange) {
    return hunting && foe && sees && distance <= wardRange;
  }
}
