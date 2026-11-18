package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GuildCounterTest {

  @ParameterizedTest(name = "{0}, contract {1} -> {2}")
  @CsvSource({
    "GEAR, false, true",
    "GEAR, true, false",
    "CONTRACTS, true, true",
    "CONTRACTS, false, false"
  })
  @DisplayName("gear and contracts are offered at separate counters, so each can be gated alone")
  void holds(final GuildCounter.Kind kind, final boolean contract, final boolean holds) {
    assertEquals(holds, kind.holds(contract));
  }

  @ParameterizedTest(name = "{0} -> {1}, {2}")
  @CsvSource({
    "GEAR, nightborn.guild.gear, minecraft:emerald",
    "CONTRACTS, nightborn.guild.contracts, nightborn:hunter_contract"
  })
  @DisplayName("each counter has its own label and icon on the wheel")
  void wheel(final GuildCounter.Kind kind, final String key, final String icon) {
    assertEquals(key, kind.translationKey());
    assertEquals(icon, kind.icon().toString());
  }

  @ParameterizedTest(name = "{0}, vampire {1}, hunter {2} -> {3}")
  @CsvSource({
    "GEAR, false, false, true",
    "GEAR, false, true, true",
    "GEAR, true, false, false",
    "CONTRACTS, false, false, false",
    "CONTRACTS, false, true, true",
    "CONTRACTS, true, false, false"
  })
  @DisplayName("any human may buy gear, but only a sworn hunter may take contracts")
  void openTo(
      final GuildCounter.Kind kind,
      final boolean vampire,
      final boolean hunter,
      final boolean open) {
    assertEquals(open, kind.openTo(vampire, hunter));
  }
}
