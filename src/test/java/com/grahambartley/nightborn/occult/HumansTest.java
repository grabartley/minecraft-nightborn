package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.blood.BloodQuality;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HumansTest {

  @ParameterizedTest(name = "blood {0}, vampire {1}: human {2}")
  @CsvSource({
    "VILLAGER, false, true",
    "PLAYER, false, true",
    "PLAYER, true, false",
    "ANIMAL, false, false",
    ", false, false"
  })
  @DisplayName("a human has a villager's or a player's warm blood")
  void human(final BloodQuality blood, final boolean vampire, final boolean human) {
    assertEquals(human, Humans.human(Optional.ofNullable(blood), vampire));
  }
}
