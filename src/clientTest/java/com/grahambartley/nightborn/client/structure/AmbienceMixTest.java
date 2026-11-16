package com.grahambartley.nightborn.client.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AmbienceMixTest {

  @ParameterizedTest(name = "volume {0} towards {1} gives {2}")
  @CsvSource({
    "0.0, 1.0, 0.025",
    "1.0, 0.0, 0.975",
    "0.5, 0.51, 0.51",
    "0.5, 0.49, 0.49",
    "0.3, 0.3, 0.3",
    "0.0, 0.0, 0.0"
  })
  @DisplayName("the loop fades a fortieth of full volume a tick and never overshoots")
  void approaches(final float volume, final float target, final float expected) {
    assertEquals(expected, AmbienceMix.approach(volume, target), 0.0001F);
  }

  @ParameterizedTest(name = "chance {0}, closeness {1}, roll {2}: {3}")
  @CsvSource({
    "0.0111, 0.5, 0.005, true",
    "0.0111, 0.5, 0.02, false",
    "0.0111, 0.0, 0.005, false",
    "1.0, 0.01, 0.999, true",
    "0.0, 1.0, 0.0, false"
  })
  @DisplayName("one-shots play only inside a zone, as often as the data's tick chance says")
  void addsOneShot(
      final double chance, final float closeness, final double roll, final boolean plays) {
    assertEquals(plays, AmbienceMix.addsOneShot(chance, closeness, roll));
  }

  @ParameterizedTest(name = "closeness {0} gives volume {1}")
  @CsvSource({"0.0, 0.35", "0.5, 0.675", "1.0, 1.0", "1.5, 1.0", "-0.5, 0.35"})
  @DisplayName("one-shots grow louder towards the heart")
  void additionVolume(final float closeness, final float expected) {
    assertEquals(expected, AmbienceMix.additionVolume(closeness), 0.0001F);
  }

  @ParameterizedTest(name = "volume {0}, target {1}: finished {2}")
  @CsvSource({"0.0, 0.0, true", "0.1, 0.0, false", "0.0, 0.4, false", "0.5, 0.5, false"})
  @DisplayName("a loop ends only once it has faded to silence with nothing to rise to")
  void finished(final float volume, final float target, final boolean finished) {
    assertEquals(finished, AmbienceMix.finished(volume, target));
  }

  private static Optional<Identifier> id(final String sound) {
    return sound == null ? Optional.empty() : Optional.of(Identifier.of("nightborn", sound));
  }

  @ParameterizedTest(name = "playing {0}, done {1}, wanted {2}, closeness {3}: {4}")
  @CsvSource({
    ", false, , 0.0, NOTHING",
    ", false, crypt, 0.0, NOTHING",
    ", false, crypt, 0.3, START",
    "crypt, false, crypt, 0.3, KEEP",
    "crypt, false, crypt, 0.0, KEEP",
    "crypt, true, crypt, 0.3, START",
    "crypt, true, crypt, 0.0, NOTHING",
    "crypt, true, , 0.0, NOTHING",
    "crypt, false, castle, 0.3, REPLACE",
    "crypt, false, castle, 0.0, STOP",
    "crypt, false, , 0.0, STOP",
    "crypt, false, , 0.5, STOP"
  })
  @DisplayName(
      "a loop starts on entering, fades on leaving, keeps going for the same sound and hands over"
          + " to another structure's")
  void loopAction(
      final String playing,
      final boolean done,
      final String wanted,
      final float closeness,
      final LoopAction expected) {
    assertEquals(expected, AmbienceMix.loopAction(id(playing), done, id(wanted), closeness));
  }
}
