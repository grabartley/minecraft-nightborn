package com.grahambartley.nightborn.wild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class NightLogTest {

  private static final long DAY = 24_000L;
  private static final UUID MASTER = new UUID(0, 5);

  @ParameterizedTest(name = "first seen before night {0} after {1} ticks played: night {2} new {3}")
  @CsvSource({
    "10, 0, 10, true",
    "10, 0, 14, true",
    "10, 0, 15, false",
    "10, 0, 9, false",
    "10, 119999, 14, true",
    "10, 120000, 10, false",
    "10, 9000000, 12, false"
  })
  @DisplayName("a player is new for their first nights, unless they played that long already")
  void isNew(final long coming, final long played, final long night, final boolean isNew) {
    assertEquals(isNew, NightLog.begun(coming, played, 5, DAY).isNew(night, 5));
  }

  @ParameterizedTest(name = "rolled {0}, asked {1}: {2}")
  @CsvSource({"3, 3, true", "3, 4, false"})
  @DisplayName("each night is rolled once")
  void rolled(final long rolledOn, final long night, final boolean rolled) {
    assertEquals(rolled, NightLog.begun(0, 0, 5, DAY).rolledOn(rolledOn).rolled(night));
  }

  @ParameterizedTest(name = "muttered {0}, asked {1}: {2}")
  @CsvSource({"7, 7, true", "7, 8, false"})
  @DisplayName("villagers mutter to a player once a day")
  void muttered(final long mutteredOn, final long day, final boolean muttered) {
    assertEquals(muttered, NightLog.begun(0, 0, 5, DAY).mutteredTo(mutteredOn).mutteredOn(day));
  }

  @ParameterizedTest(name = "greeted {0}")
  @CsvSource({"true", "false"})
  @DisplayName("the party is met once, and its master greets once")
  void party(final boolean greeted) {
    NightLog log = NightLog.begun(0, 0, 5, DAY).metParty(MASTER);
    if (greeted) {
      log = log.greeted();
    }
    assertEquals(true, log.partyMet());
    assertEquals(greeted ? Optional.empty() : Optional.of(MASTER), log.greeter());
  }

  static Stream<Arguments> logs() {
    return Stream.of(
        Arguments.of(NightLog.begun(4, 0, 5, DAY)),
        Arguments.of(NightLog.begun(4, 0, 5, DAY).rolledOn(4).watchedOnce()),
        Arguments.of(NightLog.begun(4, 0, 5, DAY).metParty(MASTER).mutteredTo(3)));
  }

  @ParameterizedTest(name = "{index}")
  @MethodSource("logs")
  @DisplayName("a player's first nights survive a save and load")
  void roundTrip(final NightLog log) {
    assertEquals(
        log,
        NightLog.CODEC
            .parse(JsonOps.INSTANCE, NightLog.CODEC.encodeStart(JsonOps.INSTANCE, log).getOrThrow())
            .getOrThrow());
  }
}
