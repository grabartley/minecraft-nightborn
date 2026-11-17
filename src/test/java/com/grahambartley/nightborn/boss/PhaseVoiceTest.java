package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PhaseVoiceTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"sound\":\"minecraft:entity.warden.angry\",\"every_ticks\":100} | true",
        "{\"sound\":\"minecraft:entity.warden.angry\",\"every_ticks\":100,\"pitch\":0.6} | true",
        "{\"every_ticks\":100} | false",
        "{\"sound\":\"minecraft:entity.warden.angry\"} | false",
        "{\"sound\":\"minecraft:entity.warden.angry\",\"every_ticks\":5} | false",
        "{\"sound\":\"minecraft:entity.warden.angry\",\"every_ticks\":100,\"pitch\":3} | false"
      })
  @DisplayName("a voice is a sound and how often it is heard")
  void parses(final String json, final boolean loads) {
    assertEquals(
        loads, PhaseVoice.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0} ticks into the phase: speaks {1}")
  @CsvSource({
    "0, false",
    "1, false",
    "99, false",
    "100, true",
    "101, false",
    "300, true",
    "-100, false"
  })
  @DisplayName("the voice is heard on its beat, never the moment the phase begins")
  void speaks(final long since, final boolean speaks) {
    final PhaseVoice voice = new PhaseVoice(Identifier.ofVanilla("entity.warden.angry"), 100, 0.6F);
    assertEquals(speaks, voice.speaksAt(since));
  }
}
