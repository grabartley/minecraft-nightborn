package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ShownActionTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "0 | false",
        "0,0 | false,false",
        "0,1 | false,true",
        "4,4,5,5,6 | false,false,true,false,true"
      })
  @DisplayName("a clip plays for each new trigger, but not for one already sent before loading")
  void fresh(final String sequences, final String expected) {
    final ShownAction action = new ShownAction();
    final List<Boolean> fresh =
        List.of(sequences.split(",")).stream()
            .map(s -> action.fresh(Integer.parseInt(s.trim())))
            .toList();
    assertEquals(List.of(expected.split(",")).stream().map(Boolean::parseBoolean).toList(), fresh);
  }

  @ParameterizedTest(name = "{0} then end {1}: showing mourn {2}")
  @CsvSource({"mourn, false, true", "mourn, true, false", "talk, false, false", "'', false, false"})
  @DisplayName(
      "the model knows when a head-holding clip is playing, so the look leaves the head alone")
  void showing(final String clip, final boolean ended, final boolean showing) {
    final ShownAction action = new ShownAction();
    action.begin(1, "");
    action.begin(2, clip);
    if (ended) {
      action.end();
    }
    assertEquals(showing, action.showing(Set.of("mourn")));
  }
}
