package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HunterVoiceTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({
    "AMBIENT, ambient",
    "HURT, hurt",
    "DEATH, death",
    "ALERT, alert",
    "STRIKE, strike",
    "CHEER, cheer"
  })
  void namesItsSoundInLowerCase(final HunterVoice voice, final String id) {
    assertEquals(id, voice.id());
  }
}
