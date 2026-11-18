package com.grahambartley.nightborn.journal;

import com.mojang.serialization.Codec;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record JournalLog(Set<Journal> given) {

  public static final JournalLog NONE = new JournalLog(Set.of());

  public static final Codec<JournalLog> CODEC =
      Journal.CODEC.listOf().xmap(given -> new JournalLog(Set.copyOf(given)), log -> log.inOrder());

  public JournalLog {
    given = Set.copyOf(given);
  }

  public JournalLog with(final Journal journal) {
    final EnumSet<Journal> next = EnumSet.of(journal);
    next.addAll(given);
    return new JournalLog(next);
  }

  public Optional<Journal> owed(final Optional<Journal> side) {
    return side.filter(journal -> !given.contains(journal));
  }

  List<Journal> inOrder() {
    final EnumSet<Journal> ordered = EnumSet.noneOf(Journal.class);
    ordered.addAll(given);
    return List.copyOf(ordered);
  }
}
