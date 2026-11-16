package com.grahambartley.nightborn.mob.look;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VampireVoiceTest {

  @ParameterizedTest(name = "{0} tall speaks at pitch {1}")
  @CsvSource({"1.75, 1.0", "1.5, 1.15", "2.0, 0.85", "1.0, 1.2", "3.0, 0.8"})
  @DisplayName("a smaller vampire speaks higher and a taller one lower, within limits")
  void pitch(final float height, final float pitch) {
    assertEquals(pitch, VampireVoice.pitch(height), 1.0E-4F);
  }

  @ParameterizedTest(name = "{0} is {1}")
  @CsvSource({
    "AMBIENT, ambient",
    "HURT, hurt",
    "DEATH, death",
    "HISS, hiss",
    "TALK, talk",
    "ARGUE, argue"
  })
  @DisplayName("each voice has a lower case id for its sound")
  void id(final VampireVoice voice, final String id) {
    assertEquals(id, voice.id());
  }
}
