package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MarkShareTest {

  @ParameterizedTest(name = "hunter {0}, ally {1}, own {2}, allied {3} -> {4}")
  @CsvSource({
    "true, false, 0.15, 0.1, 0.15",
    "true, false, 0.15, 0, 0.15",
    "false, true, 0.15, 0.1, 0.1",
    "false, true, 0.15, 0, 0",
    "false, false, 0.15, 0.1, 0",
    "true, false, 0, 0.1, 0"
  })
  @DisplayName(
      "the marking hunter takes the mark's share, an ally takes Hunter's Patience and a"
          + " stranger nothing")
  void share(
      final boolean byHunter,
      final boolean byAlly,
      final double own,
      final double allied,
      final double share) {
    assertEquals(share, MarkShare.of(byHunter, byAlly, own, allied), 1.0e-9);
  }

  @ParameterizedTest(name = "own marks {0}, ally marks {1} -> {2}")
  @CsvSource({"0, 0, 0", "1, 0, 0.15", "0, 1, 0.1", "0, 2, 0.1", "1, 2, 0.25"})
  @DisplayName("a hunter's own marks add up, and allies take one Hunter's Patience at most")
  void total(final int own, final int allies, final double total) {
    final List<Marker> markers = new ArrayList<>();
    for (int i = 0; i < own; i++) {
      markers.add(new Marker(true, false, 0.15, 0.1));
    }
    for (int i = 0; i < allies; i++) {
      markers.add(new Marker(false, true, 0.15, 0.1));
    }
    assertEquals(total, MarkShare.total(markers), 1.0e-9);
  }

  @ParameterizedTest(name = "{0} raised by {1} -> {2}")
  @CsvSource({"10, 0, 10", "10, 0.15, 11.5", "10, 0.25, 12.5", "4, -0.5, 4"})
  @DisplayName("a mark raises a blow by its share and never lowers it")
  void raise(final float amount, final double share, final float raised) {
    assertEquals(raised, MarkShare.raise(amount, share), 1.0e-4F);
  }
}
