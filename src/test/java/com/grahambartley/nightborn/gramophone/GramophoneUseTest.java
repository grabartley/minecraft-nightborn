package com.grahambartley.nightborn.gramophone;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GramophoneUseTest {

  @ParameterizedTest(name = "record {0}, holding a disc {1}: {2}")
  @CsvSource({"true, true, EJECT", "true, false, EJECT", "false, true, INSERT", "false, false,"})
  @DisplayName("using a gramophone ejects its disc, or takes one when it is empty")
  void decide(final boolean hasRecord, final boolean holdsDisc, final GramophoneUse use) {
    assertEquals(Optional.ofNullable(use), GramophoneUse.decide(hasRecord, holdsDisc));
  }
}
