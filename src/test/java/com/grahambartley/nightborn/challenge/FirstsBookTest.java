package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class FirstsBookTest {

  static final FirstsBook.Holder MINA = new FirstsBook.Holder(new UUID(1, 2), "Mina");
  static final FirstsBook.Holder ABE = new FirstsBook.Holder(new UUID(3, 4), "Abe");

  @ParameterizedTest(name = "{0}")
  @EnumSource(ServerFirst.class)
  void aFirstIsClaimedOnceAndSurvivesSaving(final ServerFirst first) {
    final FirstsBook claimed = FirstsBook.EMPTY.claim(first, MINA).orElseThrow();
    assertEquals(Optional.of(MINA), claimed.holder(first));
    assertEquals(Optional.empty(), claimed.claim(first, ABE));
    assertEquals(
        claimed,
        FirstsBook.CODEC
            .parse(
                JsonOps.INSTANCE,
                FirstsBook.CODEC.encodeStart(JsonOps.INSTANCE, claimed).getOrThrow())
            .getOrThrow());
    for (final ServerFirst other : ServerFirst.values()) {
      if (other != first) {
        assertEquals(Optional.empty(), claimed.holder(other));
      }
    }
  }
}
