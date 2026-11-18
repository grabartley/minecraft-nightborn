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
import org.junit.jupiter.params.provider.ValueSource;

class VampireLordEntityTest {

  private static final Map<Identifier, Integer> LORD = Map.of(VampireLordEntity.STAGE, 1);
  private static final Map<Identifier, Integer> CUSTOM =
      Map.of(Identifier.of("nightborn", "ancient"), 3);

  static Stream<Arguments> arrivals() {
    return Stream.of(
        Arguments.of("no arrival", null, LORD),
        Arguments.of("arrival with no odds", new HouseholdArrival(Map.of(), false), LORD),
        Arguments.of("other spawn data", new PassiveEntity.PassiveData(false), LORD),
        Arguments.of("household odds", new HouseholdArrival(CUSTOM, false), CUSTOM));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("arrivals")
  @DisplayName("a lord takes the lord stage unless its household names other odds")
  void arrival(final String name, final EntityData given, final Map<Identifier, Integer> odds) {
    assertEquals(odds, VampireLordEntity.arrival(given).stages());
  }

  @ParameterizedTest(name = "head {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("a lord stays the head of its household when it arrives as one")
  void keepsHead(final boolean head) {
    assertEquals(head, VampireLordEntity.arrival(new HouseholdArrival(Map.of(), head)).head());
    assertEquals(head, VampireLordEntity.arrival(new HouseholdArrival(CUSTOM, head)).head());
  }
}
