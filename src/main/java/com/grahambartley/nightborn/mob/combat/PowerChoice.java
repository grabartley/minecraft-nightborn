package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerUse;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;

public final class PowerChoice {

  public record Option(Identifier power, PowerUse.Verdict verdict, int cost, MobPowerUse use) {}

  private PowerChoice() {}

  public static boolean worthCasting(
      final Option option, final double distance, final int blood, final int bloodFloor) {
    return option.verdict() == PowerUse.Verdict.READY
        && option.use().inRange(distance)
        && blood - option.cost() >= bloodFloor;
  }

  public static Optional<Option> choose(
      final List<Option> options, final double distance, final int blood, final int bloodFloor) {
    return first(options, false, distance, blood, bloodFloor);
  }

  public static Optional<Option> dodge(
      final List<Option> options, final double distance, final int blood, final int bloodFloor) {
    return first(options, true, distance, blood, bloodFloor);
  }

  private static Optional<Option> first(
      final List<Option> options,
      final boolean dodge,
      final double distance,
      final int blood,
      final int bloodFloor) {
    return options.stream()
        .filter(option -> option.use().dodge() == dodge)
        .filter(option -> worthCasting(option, distance, blood, bloodFloor))
        .findFirst();
  }
}
