package com.grahambartley.nightborn.clothing;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record PowerSources(Map<ClothingPower, Map<String, Double>> amounts) {

  public static final PowerSources NONE = new PowerSources(Map.of());

  public PowerSources {
    final Map<ClothingPower, Map<String, Double>> copy = new HashMap<>();
    amounts.forEach((power, sources) -> copy.put(power, Map.copyOf(sources)));
    amounts = Map.copyOf(copy);
  }

  public PowerSources with(final ClothingPower power, final String source, final double amount) {
    final Map<ClothingPower, Map<String, Double>> next = new HashMap<>(this.amounts);
    final Map<String, Double> sources = new HashMap<>(next.getOrDefault(power, Map.of()));
    sources.put(source, amount);
    next.put(power, sources);
    return new PowerSources(next);
  }

  public PowerSources without(final ClothingPower power, final String source) {
    final Map<ClothingPower, Map<String, Double>> next = new HashMap<>(this.amounts);
    final Map<String, Double> kept = new HashMap<>(next.getOrDefault(power, Map.of()));
    kept.remove(source);
    if (kept.isEmpty()) {
      next.remove(power);
    } else {
      next.put(power, kept);
    }
    return new PowerSources(next);
  }

  public PowerSources without(final String source) {
    final Map<ClothingPower, Map<String, Double>> next = new HashMap<>();
    this.amounts.forEach(
        (power, sources) -> {
          final Map<String, Double> kept = new HashMap<>(sources);
          kept.remove(source);
          if (!kept.isEmpty()) {
            next.put(power, kept);
          }
        });
    return new PowerSources(next);
  }

  public double strongest(final ClothingPower power) {
    final Optional<Double> strongest =
        this.amounts.getOrDefault(power, Map.of()).values().stream()
            .max((a, b) -> Double.compare(Math.abs(a), Math.abs(b)));
    return strongest.orElse(0.0);
  }
}
