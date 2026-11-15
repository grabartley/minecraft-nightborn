package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.blood.BloodQuality;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class ChaliceBloodTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(BloodQuality.class)
  void keepsTheQualityPouredIn(final BloodQuality quality) {
    assertEquals(Optional.of(quality), ChaliceBlood.of(quality).quality());
  }

  @ParameterizedTest(name = "{0} is {1}")
  @CsvSource({"EMPTY, empty", "ANIMAL, animal", "VILLAGER, villager", "PLAYER, player"})
  void namesEachStateForTheBlockstate(final ChaliceBlood blood, final String id) {
    assertEquals(id, blood.asString());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = ChaliceBlood.class, names = "EMPTY")
  void anEmptyChaliceHoldsNoBlood(final ChaliceBlood blood) {
    assertTrue(blood.quality().isEmpty());
  }
}
