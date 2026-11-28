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

  @ParameterizedTest(name = "allied {0}, opposed {1}, fights us {2}, fought {3}: foe {4}")
  @CsvSource({
    "false, true, false, false, true",
    "false, false, true, false, true",
    "false, false, false, true, true",
    "false, false, false, false, false",
    "true, true, true, true, false"
  })
  @DisplayName(
      "a foe is anyone not allied who is on the other side, fights the group, or is fought by it")
  void foe(
      final boolean allied,
      final boolean opposed,
      final boolean fightsUs,
      final boolean fought,
      final boolean foe) {
    assertEquals(foe, Sides.foe(allied, opposed, fightsUs, fought));
  }
}
