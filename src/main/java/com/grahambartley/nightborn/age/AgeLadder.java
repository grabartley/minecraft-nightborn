package com.grahambartley.nightborn.age;

import com.grahambartley.nightborn.mob.stage.StageAge;
import com.grahambartley.nightborn.mob.stage.VampireStage;
import com.grahambartley.nightborn.mob.stage.VampireStages;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

public record AgeLadder(List<Rung> rungs) {

  public record Rung(Identifier id, VampireStage stage) {

    public StageAge age() {
      return stage.age().orElseThrow();
    }
  }

  public static AgeLadder of(final Map<Identifier, VampireStage> stages) {
    return new AgeLadder(
        stages.entrySet().stream()
            .filter(entry -> entry.getValue().age().isPresent())
            .map(entry -> new Rung(entry.getKey(), entry.getValue()))
            .sorted(
                Comparator.comparingInt((Rung rung) -> rung.stage().rank()).thenComparing(Rung::id))
            .toList());
  }

  public static AgeLadder of(final DynamicRegistryManager registries) {
    return of(VampireStages.stages(registries));
  }

  public int size() {
    return rungs.size();
  }

  public boolean top(final int rank) {
    return rank == rungs.size() - 1;
  }

  public Optional<Rung> rung(final int rank) {
    return rank >= 0 && rank < rungs.size() ? Optional.of(rungs.get(rank)) : Optional.empty();
  }

  public Optional<Integer> rankOf(final Identifier id) {
    for (int rank = 0; rank < rungs.size(); rank++) {
      if (rungs.get(rank).id().equals(id)) {
        return Optional.of(rank);
      }
    }
    return Optional.empty();
  }

  public double notoriety(final int rank) {
    return rung(Math.min(rank, rungs.size() - 1))
        .map(rung -> rung.age().notoriety())
        .orElse(StageAge.PLAIN_NOTORIETY);
  }

  public int stageRank(final int rank) {
    return rung(Math.min(rank, rungs.size() - 1)).map(rung -> rung.stage().rank()).orElse(0);
  }
}
