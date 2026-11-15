package com.grahambartley.nightborn.mob.stage;

import com.grahambartley.nightborn.mob.look.Weighted;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.util.Identifier;

public final class StageOdds {

  public static final int HEAVIEST = 1000;

  private StageOdds() {}

  public static Optional<Identifier> pick(
      final Map<Identifier, VampireStage> stages,
      final Map<Identifier, Integer> odds,
      final double roll) {
    return Weighted.pick(
            new TreeMap<>(stages).entrySet().stream().toList(),
            entry ->
                odds.isEmpty() ? entry.getValue().weight() : odds.getOrDefault(entry.getKey(), 0),
            roll)
        .map(Map.Entry::getKey);
  }
}
