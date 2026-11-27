package com.grahambartley.nightborn.rank;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RankBearerTest {

  @ParameterizedTest(name = "\"{0}\" -> {1}")
  @CsvSource({
    "'', ''",
    "nightborn:master, nightborn:master",
    "nightborn:initiate, nightborn:initiate"
  })
  @DisplayName("a tracked rank reads back as the rank it was written from, and blank as none")
  void roundTrips(final String tracked, final String expected) {
    final Optional<Identifier> rank = RankBearer.read(tracked);
    assertEquals(
        expected.isEmpty() ? Optional.empty() : Optional.of(Identifier.of(expected)), rank);
    assertEquals(tracked, RankBearer.write(rank));
  }

  @ParameterizedTest(name = "\"{0}\"")
  @CsvSource({"Not A Rank!", "nightborn:Bad Rank"})
  @DisplayName("a tracked value that is not an id shows no rank")
  void badValueIsNoRank(final String tracked) {
    assertEquals(Optional.empty(), RankBearer.read(tracked));
  }
}
