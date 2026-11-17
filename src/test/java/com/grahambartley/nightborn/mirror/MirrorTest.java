package com.grahambartley.nightborn.mirror;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MirrorTest {

  @ParameterizedTest(name = "vampire {0} -> shows {1}")
  @CsvSource({"true, false", "false, true"})
  @DisplayName("the glass shows a human and stays empty for a vampire")
  void reflects(final boolean vampire, final boolean shows) {
    assertEquals(shows, Mirror.reflects(vampire));
  }

  @ParameterizedTest(name = "sneaking {0} -> shows the user {1}")
  @CsvSource({"true, true", "false, false"})
  @DisplayName("raised at nobody, the mirror shows its user only while sneaking")
  void showsSelf(final boolean sneaking, final boolean shows) {
    assertEquals(shows, Mirror.showsSelf(sneaking));
  }
}
