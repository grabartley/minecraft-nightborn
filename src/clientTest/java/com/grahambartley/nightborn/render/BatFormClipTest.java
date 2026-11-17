package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class BatFormClipTest {

  @ParameterizedTest(name = "{0} while playing {1} -> {2}")
  @CsvSource({
    "HANG, FLY, HANG",
    "SWOOP, FLY, SWOOP",
    "SWOOP, REST, SWOOP",
    "CRAWL, REST, CRAWL",
    "CRAWL, FLY, CRAWL",
    "REST, FLY, SETTLE",
    "REST, SWOOP, SETTLE",
    "REST, TAKE_OFF, SETTLE",
    "REST, SETTLE, SETTLE",
    "REST, CRAWL, REST",
    "REST, REST, REST",
    "FLY, SETTLE, TAKE_OFF",
    "FLY, REST, TAKE_OFF",
    "FLY, CRAWL, TAKE_OFF",
    "FLY, TAKE_OFF, TAKE_OFF",
    "FLY, SWOOP, FLY",
    "FLY, HANG, FLY",
    "FLY, FLY, FLY"
  })
  @DisplayName("a bat settles as it lands and springs up as it takes off")
  void next(final BatFormPose pose, final BatFormClip playing, final BatFormClip expected) {
    assertEquals(expected, BatFormClip.next(pose, Optional.of(playing)));
  }

  @ParameterizedTest(name = "{0} from nothing -> {1}")
  @CsvSource({"HANG, HANG", "SWOOP, SWOOP", "CRAWL, CRAWL", "REST, SETTLE", "FLY, FLY"})
  @DisplayName("a bat that was playing nothing starts on the clip for its pose")
  void nextFromNothing(final BatFormPose pose, final BatFormClip expected) {
    assertEquals(expected, BatFormClip.next(pose, Optional.empty()));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(BatFormClip.class)
  @DisplayName("every clip is recognised again from the animation it plays")
  void playing(final BatFormClip clip) {
    assertEquals(Optional.of(clip), BatFormClip.playing(clip.clip()));
  }
}
