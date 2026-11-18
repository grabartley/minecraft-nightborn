package com.grahambartley.nightborn.hunt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BellCallTest {

  @ParameterizedTest(name = "seal {0}, window {1}, overworld {2}, sky {3}, busy {4}: calls {5}")
  @CsvSource({
    "true, true, true, true, false, true",
    "false, true, true, true, false, false",
    "true, false, true, true, false, false",
    "true, true, false, true, false, false",
    "true, true, true, false, false, false",
    "true, true, true, true, true, false"
  })
  @DisplayName("the bell calls him only with a seal, early in the night, under open sky")
  void calls(
      final boolean seal,
      final boolean window,
      final boolean overworld,
      final boolean sky,
      final boolean busy,
      final boolean calls) {
    assertEquals(calls, BellCall.calls(seal, window, overworld, sky, busy));
  }

  @ParameterizedTest(name = "tick {0} of {1}: answers {2}, done {3}")
  @CsvSource({
    "0, 200, true, false",
    "1, 200, false, false",
    "40, 200, true, false",
    "199, 200, false, false",
    "200, 200, false, true"
  })
  @DisplayName("bells answer every two seconds until he comes")
  void answers(final int elapsed, final int total, final boolean answers, final boolean done) {
    final BellCall call = new BellCall(elapsed, total);
    assertEquals(answers, call.answers());
    assertEquals(done, call.done());
  }

  @ParameterizedTest(name = "tick {0} of {1}: {2} blocks off, light {3}")
  @CsvSource({"0, 200, 48.0, 1", "100, 200, 28.0, 6", "200, 200, 8.0, 10", "5, 0, 8.0, 10"})
  @DisplayName("the answering bells come nearer and the light grows")
  void comesNearer(final int elapsed, final int total, final double distance, final int light) {
    final BellCall call = new BellCall(elapsed, total);
    assertEquals(distance, call.answerDistance(), 0.001);
    assertEquals(light, call.light());
  }

  @ParameterizedTest(name = "{0} ticks")
  @CsvSource({"20", "200", "1200"})
  @DisplayName("a ring starts the build-up from the beginning and each tick moves it on")
  void advances(final int total) {
    BellCall call = BellCall.rung(total);
    for (int tick = 0; tick < total; tick++) {
      call = call.advanced();
    }
    assertEquals(new BellCall(total, total), call);
    assertEquals(true, call.done());
  }

  @ParameterizedTest(name = "alive {0}, sky {1}, window {2}, done {3}: holds {4}")
  @CsvSource({
    "true, true, true, false, true",
    "true, true, false, false, true",
    "true, true, true, true, true",
    "true, true, false, true, false",
    "false, true, true, false, false",
    "true, false, true, false, false"
  })
  @DisplayName(
      "the call holds while the ringer lives under open sky, and he keeps trying until midnight")
  void holds(
      final boolean alive,
      final boolean sky,
      final boolean window,
      final boolean done,
      final boolean holds) {
    assertEquals(holds, BellCall.holds(alive, sky, window, done));
  }

  @ParameterizedTest(name = "tick {0} of {1}: checks {2}, tries to arrive {3}")
  @CsvSource({
    "0, 200, true, false",
    "10, 200, false, false",
    "199, 200, false, false",
    "200, 200, true, true",
    "201, 200, false, false",
    "220, 200, true, true",
    "230, 200, false, false"
  })
  @DisplayName(
      "checks run once a second, and once the bells are done he tries to arrive each second")
  void retries(final int elapsed, final int total, final boolean checks, final boolean tries) {
    final BellCall call = new BellCall(elapsed, total);
    assertEquals(checks, call.checks());
    assertEquals(tries, call.triesArrival());
  }
}
