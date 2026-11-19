package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillPowerGiftsTest {

  @ParameterizedTest(name = "power of {0}: {1} gifts")
  @CsvSource({"nightborn:beast, 2", "nightborn:noble, 1", "nightborn:occult, 0"})
  @DisplayName("only gifts for the power's own bloodline apply to it")
  void covering(final String branch, final int count) {
    final List<PowerGift> gifts =
        List.of(
            new PowerGift(Identifier.of("nightborn:beast"), 1, 0.2, false),
            new PowerGift(Identifier.of("nightborn:beast"), 0, 0.1, false),
            new PowerGift(Identifier.of("nightborn:noble"), 0, 0.2, false));
    assertEquals(count, SkillPowerGifts.covering(Identifier.of(branch), gifts).size());
  }
}
