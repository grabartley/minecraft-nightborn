package com.grahambartley.nightborn.name;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NameBearerTest {

  @ParameterizedTest(name = "female={0}")
  @ValueSource(booleans = {false, true})
  @DisplayName("a bearer with nothing of its own has no epithet, colour or namer")
  void defaults(final boolean female) {
    final NameBearer bearer = () -> female;
    bearer.bearEpithet("the Pale");
    assertEquals(female, bearer.bearsWomansName());
    assertEquals("", bearer.epithet());
    assertEquals(Optional.empty(), bearer.nameColor());
    assertEquals(Optional.empty(), bearer.namedBy());
  }
}
