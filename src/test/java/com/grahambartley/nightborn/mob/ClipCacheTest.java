package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ClipCacheTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"leap", "idle_elder", "blood_rush_tell"})
  @DisplayName("each clip is built once per way of playing it")
  void caches(final String clip) {
    assertSame(ClipCache.once(clip), ClipCache.once(clip));
    assertSame(ClipCache.loop(clip), ClipCache.loop(clip));
    assertNotSame(ClipCache.once(clip), ClipCache.loop(clip));
  }
}
