package com.grahambartley.nightborn.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.Set;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BossMusicEndTest {

  private static final Identifier ELDEST = Identifier.of("nightborn", "music.boss.eldest");
  private static final Identifier FINAL = Identifier.of("nightborn", "music.boss.inquisitor_final");
  private static final Identifier NIGHT = Identifier.of("nightborn", "music.gothic");
  private static final Set<Identifier> BOSS_TRACKS = Set.of(ELDEST, FINAL);

  private static Optional<Identifier> track(final String name) {
    return switch (name) {
      case "eldest" -> Optional.of(ELDEST);
      case "final" -> Optional.of(FINAL);
      case "night" -> Optional.of(NIGHT);
      default -> Optional.empty();
    };
  }

  @ParameterizedTest(name = "playing {0}, arena holds {1}: stops {2}")
  @CsvSource({
    "eldest, none, true",
    "final, none, true",
    "eldest, eldest, false",
    "final, final, false",
    "eldest, final, false",
    "night, none, false",
    "night, eldest, false",
    "none, none, false",
    "none, eldest, false"
  })
  @DisplayName("a boss track stops once the arena holds no boss music, and nothing else does")
  void stops(final String playing, final String held, final boolean stops) {
    assertEquals(stops, BossMusicEnd.stops(track(playing).orElse(null), BOSS_TRACKS, track(held)));
  }
}
