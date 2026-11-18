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
    final Challenge challenge = new Challenge(challenger, night);
    assertEquals(
        challenge,
        Challenge.CODEC
            .parse(
                JsonOps.INSTANCE,
                Challenge.CODEC.encodeStart(JsonOps.INSTANCE, challenge).getOrThrow())
            .getOrThrow());
  }
}
