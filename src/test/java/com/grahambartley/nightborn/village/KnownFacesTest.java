package com.grahambartley.nightborn.village;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class KnownFacesTest {

  @ParameterizedTest(name = "known {0}, charmed by them {1}: shunned {2}")
  @CsvSource({
    "true, false, true",
    "true, true, false",
    "false, false, false",
    "false, true, false"
  })
  @DisplayName("villagers shun a known face unless that face has charmed them")
  void shuns(final boolean known, final boolean charmed, final boolean shuns) {
    assertEquals(shuns, KnownFaces.shuns(known, charmed));
  }
}
