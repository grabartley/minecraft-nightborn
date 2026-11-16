package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillBareHandsTest {

  private static Skill skill(final double bareHands) {
    final Identifier id = Identifier.of("nightborn", "beast");
    return new Skill(id, 1, List.of(), id, 0, 0, List.of(), bareHands, Optional.empty());
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"'', 0", "'0', 0", "'2', 2", "'2,1.5', 3.5", "'0,2,0', 2"})
  @DisplayName("the bare hand bonus is the sum over every owned skill")
  void total(final String bonuses, final double expected) {
    final List<Skill> skills =
        bonuses.isEmpty()
            ? List.of()
            : Arrays.stream(bonuses.split(","))
                .map(value -> skill(Double.parseDouble(value)))
                .toList();
    assertEquals(expected, SkillBareHands.total(skills), 0.0001);
  }
}
