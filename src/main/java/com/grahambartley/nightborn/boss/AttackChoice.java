package com.grahambartley.nightborn.boss;

import java.util.List;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import net.minecraft.util.Identifier;

public final class AttackChoice {

  private AttackChoice() {}

  public record Option(Identifier id, BossAttack attack, long readyAt) {}

  public static Optional<Option> choose(
      final List<Option> options,
      final double distance,
      final long now,
      final IntUnaryOperator roll) {
    final List<Option> open =
        options.stream()
            .filter(option -> now >= option.readyAt() && option.attack().reaches(distance))
            .toList();
    final int total = open.stream().mapToInt(option -> option.attack().weight()).sum();
    if (total <= 0) {
      return Optional.empty();
    }
    int left = Math.floorMod(roll.applyAsInt(total), total);
    for (final Option option : open) {
      left -= option.attack().weight();
      if (left < 0) {
        return Optional.of(option);
      }
    }
    return Optional.empty();
  }
}
