package com.grahambartley.nightborn.ward;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class WardPointsTest {

  static Stream<Arguments> points() {
    return Stream.of(
        Arguments.of(WardPoints.GARLIC_STRING, WardPoints.GARLIC_STRING_ID, "garlic_string"),
        Arguments.of(WardPoints.HUNG_CRUCIFIX, WardPoints.HUNG_CRUCIFIX_ID, "hung_crucifix"));
  }

  @ParameterizedTest(name = "{2}")
  @MethodSource("points")
  void eachWardIsItsOwnPointOfInterest(
      final RegistryKey<PointOfInterestType> key, final Identifier id, final String path) {
    assertEquals(Identifier.of("nightborn", path), id);
    assertEquals(id, key.getValue());
  }
}
