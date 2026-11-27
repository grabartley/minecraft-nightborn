package com.grahambartley.nightborn.rank;

import java.util.Optional;
import net.minecraft.util.Identifier;

public interface RankBearer {

  Optional<Identifier> shownRank();

  static Optional<Identifier> read(final String tracked) {
    return tracked.isEmpty() ? Optional.empty() : Optional.ofNullable(Identifier.tryParse(tracked));
  }

  static String write(final Optional<Identifier> rank) {
    return rank.map(Identifier::toString).orElse("");
  }
}
