package com.grahambartley.nightborn.mob.look;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.DoubleSupplier;
import net.minecraft.util.Identifier;

public record Appearance(Identifier look, Optional<Identifier> outfit, float height, float width) {

  public static Optional<Appearance> pick(
      final Map<Identifier, VampireLook> looks,
      final Map<Identifier, VampireOutfit> outfits,
      final Map<Identifier, VampireBuild> builds,
      final DoubleSupplier rolls) {
    final var lookEntries = new TreeMap<>(looks).entrySet().stream().toList();
    return Weighted.pick(lookEntries, e -> e.getValue().weight(), rolls.getAsDouble())
        .map(
            look -> {
              final var fitting =
                  new TreeMap<>(outfits)
                      .entrySet().stream().filter(e -> e.getValue().fits(look.getValue())).toList();
              final Optional<Identifier> outfit =
                  Weighted.pick(fitting, e -> e.getValue().weight(), rolls.getAsDouble())
                      .map(Map.Entry::getKey);
              final Optional<VampireBuild> build =
                  Weighted.pick(
                      new TreeMap<>(builds).values().stream().toList(),
                      VampireBuild::weight,
                      rolls.getAsDouble());
              final double heightRoll = rolls.getAsDouble();
              return new Appearance(
                  look.getKey(),
                  outfit,
                  build.map(b -> b.height(heightRoll)).orElse(VampireSize.BASE_HEIGHT),
                  build.map(VampireBuild::width).orElse(1.0F));
            });
  }
}
