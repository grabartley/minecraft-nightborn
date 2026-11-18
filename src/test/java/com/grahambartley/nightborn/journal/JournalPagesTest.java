package com.grahambartley.nightborn.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class JournalPagesTest {

  private static List<Optional<Identifier>> gates(final String text) {
    return Arrays.stream(text.split(" "))
        .map(
            gate ->
                gate.equals("-") ? Optional.<Identifier>empty() : Optional.of(Identifier.of(gate)))
        .toList();
  }

  private static Set<Identifier> ids(final String text) {
    return Arrays.stream(text.split(" "))
        .filter(id -> !id.isEmpty())
        .map(Identifier::of)
        .collect(Collectors.toSet());
  }

  @ParameterizedTest(name = "pages {0}, earned {1}, hidden {2} -> {3}%")
  @CsvSource({
    "a:x a:x a:y, '', '', 0",
    "a:x a:x a:y, a:x, '', 66",
    "a:x a:x a:y, a:x a:y, '', 100",
    "a:x a:y a:secret, a:x a:y, a:secret, 100",
    "- a:x, '', '', 50",
    "a:secret, '', a:secret, 0"
  })
  void sharesCountOnlyPagesBehindOrdinaryAdvancements(
      final String pages, final String earned, final String hidden, final int share) {
    final Set<Identifier> open = ids(earned);
    final Set<Identifier> secret = ids(hidden);
    assertEquals(
        share, JournalPages.share(gates(pages), id -> !secret.contains(id), open::contains));
  }

  @ParameterizedTest(name = "hidden {0}, {1} -> {2}")
  @CsvSource({
    "false, TASK, true",
    "false, GOAL, true",
    "false, CHALLENGE, false",
    "true, TASK, false"
  })
  void hiddenAndChallengePagesAreNotCounted(
      final boolean hidden, final AdvancementFrame frame, final boolean counted) {
    assertEquals(counted, JournalPages.counted(hidden, frame));
  }
}
