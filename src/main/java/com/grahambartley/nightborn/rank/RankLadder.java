package com.grahambartley.nightborn.rank;

import com.grahambartley.nightborn.faction.Oath;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

public record RankLadder(List<Rung> rungs) {

  public record Rung(Identifier id, GuildRank rank) {}

  public RankLadder {
    rungs = List.copyOf(rungs);
  }

  public static RankLadder of(final Map<Identifier, GuildRank> ranks) {
    return new RankLadder(
        ranks.entrySet().stream()
            .map(entry -> new Rung(entry.getKey(), entry.getValue()))
            .sorted(
                Comparator.comparingInt((Rung rung) -> rung.rank().order()).thenComparing(Rung::id))
            .limit(Oath.TOP_RANK + 1L)
            .toList());
  }

  public static RankLadder of(final DynamicRegistryManager registries) {
    return of(RankData.ranks(registries));
  }

  public int size() {
    return rungs.size();
  }

  public Optional<Rung> rung(final int rank) {
    return rank >= 0 && rank < rungs.size() ? Optional.of(rungs.get(rank)) : Optional.empty();
  }

  public OptionalInt rankOf(final Identifier id) {
    for (int rank = 0; rank < rungs.size(); rank++) {
      if (rungs.get(rank).id().equals(id)) {
        return OptionalInt.of(rank);
      }
    }
    return OptionalInt.empty();
  }

  public Optional<RankDeed> deed(final int rank) {
    return rung(rank).flatMap(rung -> rung.rank().deed());
  }

  public Optional<Identifier> markOf(final Oath oath) {
    return oath.sworn() ? rung(oath.rank()).map(Rung::id) : Optional.empty();
  }
}
