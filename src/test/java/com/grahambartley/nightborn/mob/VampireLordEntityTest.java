package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.structure.HouseholdArrival;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VampireLordEntityTest {

  private static final Map<Identifier, Integer> LORD = Map.of(VampireLordEntity.STAGE, 1);
  private static final Map<Identifier, Integer> CUSTOM =
      Map.of(Identifier.of("nightborn", "ancient"), 3);

  static Stream<Arguments> arrivals() {
    return Stream.of(
        Arguments.of("no arrival", null, LORD),
        Arguments.of("arrival with no odds", new HouseholdArrival(Map.of()), LORD),
        Arguments.of("other spawn data", new PassiveEntity.PassiveData(false), LORD),
        Arguments.of("household odds", new HouseholdArrival(CUSTOM), CUSTOM));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("arrivals")
  @DisplayName("a lord takes the lord stage unless its household names other odds")
  void arrival(final String name, final EntityData given, final Map<Identifier, Integer> odds) {
    assertEquals(odds, VampireLordEntity.arrival(given).stages());
  }
}
