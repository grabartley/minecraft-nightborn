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
    return options.stream()
        .filter(option -> worthCasting(option, distance, blood, bloodFloor))
        .findFirst();
  }
}
