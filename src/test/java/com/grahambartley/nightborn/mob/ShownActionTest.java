package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    action.begin(1, "", false);
    action.begin(2, clip, false);
    if (ended) {
      action.end();
    }
    assertEquals(showing, action.showing(Set.of("mourn")));
  }

  @ParameterizedTest(name = "current {0}, blank clip {1}: stops {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("a blank clip sent after a looping one stops it")
  void stops(final boolean current, final boolean blank, final boolean stops) {
    assertEquals(stops, ShownAction.stops(current, blank));
  }

  @ParameterizedTest(name = "yields {0}, playing {1}, calm {2}: held {3}")
  @CsvSource({
    "false, false, false, false",
    "false, true, false, false",
    "true, false, true, false",
    "true, true, true, true",
    "true, false, false, true",
    "true, true, false, true"
  })
  @DisplayName("a recoil is held back while another clip plays or the vampire is busy")
  void holds(final boolean yields, final boolean playing, final boolean calm, final boolean held) {
    assertEquals(held, ShownAction.holds(yields, playing, calm));
  }

  @ParameterizedTest(name = "held {0}: begins {1}, still showing tell {2}")
  @CsvSource({"false, true, false", "true, false, true"})
  @DisplayName("a held recoil leaves the power tell it arrived during showing")
  void heldRecoilKeepsTell(final boolean held, final boolean begins, final boolean tell) {
    final ShownAction action = new ShownAction();
    action.begin(1, "", false);
    action.begin(2, "blood_frenzy_tell", false);
    assertEquals(begins, action.begin(3, "recoil_garlic", held));
    assertEquals(tell, action.showing(Set.of("blood_frenzy_tell")));
    assertTrue(action.playing());
  }
}
