package com.grahambartley.nightborn.faction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SidesTest {

  @ParameterizedTest(
      name = "hunter {0}, vampire {1} with hunter {2}, vampire {3}: same {4}, opposed {5}")
  @CsvSource({
    "true, false, true, false, true, false",
    "false, true, false, true, true, false",
    "true, false, false, true, false, true",
    "false, true, true, false, false, true",
    "false, false, false, false, false, false",
    "true, false, false, false, false, false",
    "false, true, false, false, false, false"
  })
  @DisplayName(
      "hunters stand with hunters and vampires with vampires, each against the other, and anyone"
          + " else is on neither side")
  void sides(
      final boolean hunter,
      final boolean vampire,
      final boolean otherHunter,
      final boolean otherVampire,
      final boolean same,
      final boolean opposed) {
    assertEquals(same, Sides.sameSide(hunter, vampire, otherHunter, otherVampire));
    assertEquals(opposed, Sides.opposed(hunter, vampire, otherHunter, otherVampire));
  }

  @ParameterizedTest(name = "self {0}, same side {1}, teammate {2}, fighting {3}: allied {4}")
  @CsvSource({
    "false, true, false, false, true",
    "false, false, true, false, true",
    "false, false, false, false, false",
    "true, true, true, false, false",
    "false, true, false, true, false",
    "false, false, true, true, false"
  })
  @DisplayName(
      "an ally is someone else on the same side or team who is not fighting the mob, whichever"
          + " side it is on")
  void allied(
      final boolean self,
      final boolean sameSide,
      final boolean teammate,
      final boolean fighting,
      final boolean allied) {
    assertEquals(allied, Sides.allied(self, sameSide, teammate, fighting));
  }

  @ParameterizedTest(name = "opposed {0}, fights us {1}, fought {2}: foe {3}")
  @CsvSource({
    "true, false, false, true",
    "false, true, false, true",
    "false, false, true, true",
    "false, false, false, false"
  })
  @DisplayName(
      "of those not allied, a foe is one on the other side, one that fights the group, or one it"
          + " fights")
  void foe(final boolean opposed, final boolean fightsUs, final boolean fought, final boolean foe) {
    assertEquals(foe, Sides.foe(opposed, fightsUs, fought));
  }

  @ParameterizedTest(name = "boss {0}, allied {1}: spared {2}")
  @CsvSource({
    "false, true, true",
    "false, false, false",
    "true, true, false",
    "true, false, false"
  })
  @DisplayName(
      "an area power spares the caster's allies, except a boss's, which reaches as it always has")
  void spared(final boolean boss, final boolean allied, final boolean spared) {
    assertEquals(spared, Sides.spared(boss, allied));
  }
}
