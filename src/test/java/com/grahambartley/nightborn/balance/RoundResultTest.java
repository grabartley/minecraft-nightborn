package com.grahambartley.nightborn.balance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RoundResultTest {

  @ParameterizedTest(name = "{0} ticks last {1} seconds")
  @CsvSource({"20, 1.0", "410, 20.5", "0, 0.0"})
  void seconds(final int ticks, final double seconds) {
    final RoundResult result =
        new RoundResult(
            "adult-hunter-1",
            Optional.empty(),
            ticks,
            Map.of(Side.VAMPIRES, new Tally.Blows(1, 2.0F, 0, 0.0F)),
            Map.of(Side.HUNTERS, Set.of("hunter kit nightborn:stake")));
    assertEquals(seconds, result.seconds(), 1e-9);
    assertEquals(Tally.Blows.NONE, result.blowsOf(Side.HUNTERS));
    assertEquals(Set.of(), result.tagsOf(Side.VAMPIRES));
  }
}
