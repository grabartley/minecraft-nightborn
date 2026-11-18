package com.grahambartley.nightborn.house;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChallengeTest {

  @ParameterizedTest(name = "{0} on night {1}")
  @CsvSource({
    "00000000-0000-0000-0000-000000000002, 0",
    "123e4567-e89b-12d3-a456-426614174000, -5",
    "123e4567-e89b-12d3-a456-426614174001, 9000000000"
  })
  @DisplayName("a challenge keeps its challenger and its night through a save")
  void roundTrip(final UUID challenger, final long night) {
    final Challenge challenge = Challenge.declared(challenger, night);
    assertEquals(
        challenge,
        Challenge.CODEC
            .parse(
                JsonOps.INSTANCE,
                Challenge.CODEC.encodeStart(JsonOps.INSTANCE, challenge).getOrThrow())
            .getOrThrow());
  }

  @ParameterizedTest(name = "declared night 4, offered on {0} then {1} -> {2}")
  @CsvSource({"4, 4, 0", "5, 5, 1", "5, 6, 2", "6, 5, 1", "3, 9, 1"})
  @DisplayName("each night after the challenge counts once, the night it was made never")
  void offeredOn(final long first, final long second, final int offered) {
    final Challenge challenge =
        Challenge.declared(UUID.randomUUID(), 4L).offeredOn(first).offeredOn(second);
    assertEquals(offered, challenge.offered());
  }

  @ParameterizedTest(name = "offered {0}, last {1}")
  @CsvSource({"0, 4", "3, 9"})
  @DisplayName("the nights offered survive a save")
  void keepsOffered(final int offered, final long last) {
    final Challenge challenge = new Challenge(UUID.randomUUID(), 4L, offered, last);
    assertEquals(
        challenge,
        Challenge.CODEC
            .parse(
                JsonOps.INSTANCE,
                Challenge.CODEC.encodeStart(JsonOps.INSTANCE, challenge).getOrThrow())
            .getOrThrow());
  }
}
