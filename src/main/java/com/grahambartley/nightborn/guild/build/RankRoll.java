package com.grahambartley.nightborn.guild.build;

import com.grahambartley.nightborn.mob.look.Weighted;
import com.grahambartley.nightborn.rank.RankLadder;
import com.grahambartley.nightborn.rank.RankNeeds;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import net.minecraft.util.Identifier;

public final class RankRoll {

  private RankRoll() {}

  public static int rank(
      final RankLadder ladder, final Map<Identifier, Integer> weights, final double roll) {
    return Weighted.pick(
            IntStream.range(0, ladder.size()).boxed().toList(),
            rank -> ladder.rung(rank).map(rung -> weights.getOrDefault(rung.id(), 0)).orElse(0),
            roll)
        .orElse(0);
  }

  public static int points(
      final RankNeeds needs, final int rank, final int topSpread, final double roll) {
    final int low = needs.pointsFor(rank).orElse(0);
    final OptionalInt next = needs.pointsFor(rank + 1);
    final int high = Math.max(low, next.isPresent() ? next.getAsInt() - 1 : low + topSpread);
    final double clamped = Math.max(0.0, Math.min(roll, Math.nextDown(1.0)));
    return low + (int) (clamped * (high - low + 1));
  }
}
