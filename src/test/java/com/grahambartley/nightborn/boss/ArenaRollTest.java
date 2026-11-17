package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ArenaRollTest {

  private static Set<UUID> players(final String ids) {
    return ids == null || ids.isBlank()
        ? Set.of()
        : Arrays.stream(ids.split(" "))
            .map(id -> new UUID(0L, Long.parseLong(id)))
            .collect(Collectors.toSet());
  }

  @ParameterizedTest(name = "[{0}] then [{1}]: entered [{2}], left [{3}]")
  @CsvSource({
    "'', '1 2', '1 2', ''",
    "'1 2', '1 2', '', ''",
    "'1 2', '2 3', '3', '1'",
    "'1 2', '', '', '1 2'",
    "'', '', '', ''"
  })
  @DisplayName("each step reports who came in and who went out")
  void steps(final String before, final String now, final String entered, final String left) {
    final ArenaRoll roll = new ArenaRoll();
    roll.step(players(before));
    final ArenaRoll.Change change = roll.step(players(now));
    assertEquals(players(entered), change.entered());
    assertEquals(players(left), change.left());
    assertEquals(players(entered).isEmpty() && players(left).isEmpty(), change.none());
    assertEquals(players(now), roll.inside());
  }

  @Test
  @DisplayName("clearing hands back everyone inside and forgets them")
  void clears() {
    final ArenaRoll roll = new ArenaRoll();
    roll.step(players("1 2"));
    assertEquals(players("1 2"), roll.clear());
    assertEquals(Set.of(), roll.inside());
    assertEquals(players("1"), roll.step(players("1")).entered());
  }
}
