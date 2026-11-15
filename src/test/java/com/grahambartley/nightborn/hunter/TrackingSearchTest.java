package com.grahambartley.nightborn.hunter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TrackingSearchTest {

  static final Vec3d NEAR = new Vec3d(5, 0, 0);
  static final Vec3d MIDDLE = new Vec3d(0, 0, 20);
  static final Vec3d EDGE = new Vec3d(0, 0, 64);
  static final Vec3d FAR = new Vec3d(65, 0, 0);

  static Stream<Arguments> cases() {
    return Stream.of(
        Arguments.of(List.of(), 64, Optional.empty()),
        Arguments.of(List.of(FAR), 64, Optional.empty()),
        Arguments.of(List.of(EDGE), 64, Optional.of(EDGE)),
        Arguments.of(List.of(MIDDLE, NEAR, FAR), 64, Optional.of(NEAR)),
        Arguments.of(List.of(FAR, MIDDLE), 64, Optional.of(MIDDLE)),
        Arguments.of(List.of(MIDDLE, NEAR), 4, Optional.empty()));
  }

  @ParameterizedTest(name = "{0} within {1}: {2}")
  @MethodSource("cases")
  @DisplayName("the nearest candidate within range is found, and nothing beyond it")
  void nearest(final List<Vec3d> candidates, final int range, final Optional<Vec3d> found) {
    assertEquals(found, TrackingSearch.nearest(Vec3d.ZERO, candidates, Function.identity(), range));
  }
}
