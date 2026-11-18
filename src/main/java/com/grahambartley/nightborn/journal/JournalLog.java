package com.grahambartley.nightborn.journal;

import com.mojang.serialization.Codec;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record JournalLog(Set<Journal> given) {

  public static final JournalLog NONE = new JournalLog(Set.of());

  public static final Codec<JournalLog> CODEC =
      Journal.CODEC.listOf().xmap(JournalLog::new, log -> List.copyOf(log.given()));

  public JournalLog(final List<Journal> given) {
    this(given.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(given)));
  }

  public JournalLog {
    given = Set.copyOf(given);
  }

  public JournalLog with(final Journal journal) {
    final Set<Journal> next =
        given.isEmpty() ? EnumSet.noneOf(Journal.class) : EnumSet.copyOf(given);
    next.add(journal);
    return new JournalLog(next);
  }

  public Optional<Journal> owed(final Optional<Journal> side) {
    return side.filter(journal -> !given.contains(journal));
  }
}
