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

class VampireBossEntityTest {

  private static final Map<Identifier, Integer> CUSTOM =
      Map.of(Identifier.of("nightborn", "ancient"), 3);

  static Stream<Arguments> arrivals() {
    return Stream.of(VampireLordEntity.STAGE, EldestEntity.STAGE)
        .flatMap(
            stage ->
                Stream.of(
                    Arguments.of(stage, "no arrival", null, Map.of(stage, 1)),
                    Arguments.of(
                        stage,
                        "arrival with no odds",
                        new HouseholdArrival(Map.of()),
                        Map.of(stage, 1)),
                    Arguments.of(
                        stage,
                        "other spawn data",
                        new PassiveEntity.PassiveData(false),
                        Map.of(stage, 1)),
                    Arguments.of(stage, "household odds", new HouseholdArrival(CUSTOM), CUSTOM)));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @MethodSource("arrivals")
  @DisplayName("a boss takes its own stage unless its household names other odds")
  void arrival(
      final Identifier stage,
      final String name,
      final EntityData given,
      final Map<Identifier, Integer> odds) {
    assertEquals(odds, VampireBossEntity.arrival(given, stage).stages());
  }
}
