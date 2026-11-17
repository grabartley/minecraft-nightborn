package com.grahambartley.nightborn.name;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NameBearerTest {

  @ParameterizedTest(name = "offered \"{0}\"")
  @ValueSource(strings = {"the Pale", "", "the Eldest"})
  @DisplayName("a bearer with nothing of its own is no woman and has no epithet, colour or namer")
  void defaults(final String offered) {
    final NameBearer bearer = new NameBearer() {};
    bearer.bearEpithet(offered);
    assertEquals(false, bearer.bearsWomansName());
    assertEquals("", bearer.epithet());
    assertEquals(Optional.empty(), bearer.nameColor());
    assertEquals(Optional.empty(), bearer.namedBy());
  }
}
