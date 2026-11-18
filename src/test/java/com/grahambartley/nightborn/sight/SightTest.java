package com.grahambartley.nightborn.sight;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SightTest {

  @ParameterizedTest(name = "until {0}, now {1}: over {2}")
  @CsvSource({"100, 99, false", "100, 100, true"})
  @DisplayName("a sight ends at its time, whether it senses or marks")
  void over(final long until, final long now, final boolean over) {
    assertEquals(over, Sight.sense(24.0F, until).over(now));
    assertEquals(over, Sight.mark(UUID.randomUUID(), 0, until).over(now));
  }

  @ParameterizedTest(name = "sense {0}: marks one {1}")
  @CsvSource({"true, false", "false, true"})
  @DisplayName("a sense reveals an area and a mark reveals one creature")
  void kinds(final boolean sense, final boolean marks) {
    final Sight sight = sense ? Sight.sense(24.0F, 10L) : Sight.mark(UUID.randomUUID(), 1, 10L);
    assertEquals(marks, sight.marked().isPresent());
    assertEquals(sense, sight.senseRange() > 0.0F);
  }
}
