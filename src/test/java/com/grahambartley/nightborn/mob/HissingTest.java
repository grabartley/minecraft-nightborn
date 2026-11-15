package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.ward.Wards;
import com.grahambartley.nightborn.weakened.WeakenedSources;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HissingTest {

  @ParameterizedTest(name = "at {0} with next allowed {1}: {2}")
  @CsvSource({"0, 0, true", "99, 100, false", "100, 100, true", "500, 100, true"})
  @DisplayName("a vampire hisses again only once its wait has passed")
  void ready(final long now, final long next, final boolean ready) {
    assertEquals(ready, Hissing.ready(now, next));
  }

  @ParameterizedTest(name = "at {0}, {1} seconds: next at {2}")
  @CsvSource({"0, 8, 160", "1000, 0, 1000", "50, 600, 12050"})
  @DisplayName("the wait between hisses is the server's setting in seconds")
  void nextAllowed(final long now, final int seconds, final long next) {
    assertEquals(next, Hissing.nextAllowed(now, seconds));
  }

  @ParameterizedTest(name = "{0} at {1}: warded {2}")
  @CsvSource({
    Wards.STRINGS + ", 2, true",
    Wards.TOUCH + ", 1, true",
    Wards.CRUCIFIX + ", 3, true",
    Wards.SHAPE + ", 2, true",
    "sun, 4, false",
    Wards.CRUCIFIX + ", 0, false"
  })
  @DisplayName("garlic and crucifixes make a vampire hiss, other weakness does not")
  void warded(final String source, final int strength, final boolean warded) {
    assertEquals(warded, Hissing.warded(WeakenedSources.NONE.with(source, strength, 20)));
  }
}
