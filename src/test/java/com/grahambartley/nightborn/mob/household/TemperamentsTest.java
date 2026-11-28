package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Optional;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TemperamentsTest {

  private static final Map<Identifier, Temperament> TEMPERAMENTS =
      Map.of(
          Identifier.of("nightborn", "brooding"), weighing(3),
          Identifier.of("nightborn", "restless"), weighing(1));

  @ParameterizedTest(name = "roll {0}: {1}")
  @CsvSource({"0.0, brooding", "0.74, brooding", "0.76, restless", "0.999, restless"})
  @DisplayName("a vampire's temperament is picked by weight")
  void picksByWeight(final double roll, final String id) {
    assertEquals(
        Optional.of(Identifier.of("nightborn", id)), Temperaments.pick(TEMPERAMENTS, roll));
  }

  @ParameterizedTest(name = "roll {0}")
  @CsvSource({"0.0", "0.5", "0.999"})
  @DisplayName("with no temperament data a vampire is even tempered")
  void fallsBackToEven(final double roll) {
    assertEquals(Optional.empty(), Temperaments.pick(Map.of(), roll));
    assertEquals(
        Temperament.EVEN, Temperaments.of(DynamicRegistryManager.EMPTY, Temperaments.FALLBACK));
  }

  private static Temperament weighing(final int weight) {
    return new Temperament(
        weight,
        Temperament.EVEN.hobbies(),
        0,
        Temperament.EVEN.pastimeSeconds(),
        0,
        Temperament.EVEN.disposition());
  }
}
