package com.grahambartley.nightborn.coating;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoatingMixTest {

  private static CoatingMix.Role role(final String slot) {
    return switch (slot.charAt(0)) {
      case 'H' -> new CoatingMix.Role.Agent(Coating.HOLY_WATER);
      case 'G' -> new CoatingMix.Role.Agent(Coating.GARLIC);
      case 'W' -> new CoatingMix.Role.Weapon();
      case 'A' -> new CoatingMix.Role.Ammo();
      case '.' -> new CoatingMix.Role.Empty();
      default -> new CoatingMix.Role.Other();
    };
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "'W H . . . . . . .', 'HOLY_WATER 0 1'",
    "'. G . W . . . . .', 'GARLIC 3 1'",
    "'Aa Aa Aa Aa H Aa Aa Aa Aa', 'HOLY_WATER 0 8'",
    "'. . Ab G . . . . .', 'GARLIC 2 1'",
    "'Aa Aa Aa Aa H Aa Aa Aa Aa Aa', ''",
    "'Aa Ab H . . . . . .', ''",
    "'W Aa H . . . . . .', ''",
    "'W W H . . . . . .', ''",
    "'W H G . . . . . .', ''",
    "'W . . . . . . . .', ''",
    "'H . . . . . . . .', ''",
    "'W H X . . . . . .', ''"
  })
  @DisplayName("one coating agent coats one weapon or up to eight matching ammo")
  void mixes(final String grid, final String expected) {
    final List<String> slots = Arrays.asList(grid.split(" "));
    final Optional<CoatingMix> mix = CoatingMix.of(slots, CoatingMixTest::role, String::equals);
    assertEquals(
        expected.isEmpty()
            ? Optional.empty()
            : Optional.of(
                new CoatingMix(
                    Coating.valueOf(expected.split(" ")[0]),
                    Integer.parseInt(expected.split(" ")[1]),
                    Integer.parseInt(expected.split(" ")[2]))),
        mix);
  }
}
