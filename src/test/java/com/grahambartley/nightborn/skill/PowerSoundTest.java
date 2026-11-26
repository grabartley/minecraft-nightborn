package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PowerSoundTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "\"minecraft:block.bell.use\" | 1.0 | 1.0 | 0.1",
        "{\"id\":\"minecraft:block.bell.use\"} | 1.0 | 1.0 | 0.1",
        "{\"id\":\"minecraft:block.bell.use\",\"volume\":0.6} | 0.6 | 1.0 | 0.1",
        "{\"id\":\"minecraft:block.bell.use\",\"volume\":0.5,\"pitch\":1.3,\"pitch_spread\":0.0} | 0.5 | 1.3 | 0.0"
      })
  @DisplayName("a power's sound reads as a bare id or with its own volume, pitch and spread")
  void parses(final String json, final float volume, final float pitch, final float pitchSpread) {
    final PowerSound sound =
        PowerSound.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(Identifier.ofVanilla("block.bell.use"), sound.id());
    assertEquals(volume, sound.volume(), 1.0E-6F);
    assertEquals(pitch, sound.pitch(), 1.0E-6F);
    assertEquals(pitchSpread, sound.pitchSpread(), 1.0E-6F);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"id\":\"minecraft:block.bell.use\",\"volume\":1.5}",
        "{\"id\":\"minecraft:block.bell.use\",\"volume\":0.0}",
        "{\"id\":\"minecraft:block.bell.use\",\"pitch\":3.0}",
        "{\"id\":\"minecraft:block.bell.use\",\"pitch_spread\":0.9}"
      })
  @DisplayName(
      "a power's sound is never louder than full volume, and its pitch and spread stay in range")
  void refusesOutOfRange(final String json) {
    assertTrue(
        PowerSound.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError(), json);
  }

  @ParameterizedTest(name = "pitch {0} spread {1} roll {2} gives {3}")
  @CsvSource({
    "1.2, 0.1, 0.0, 1.1",
    "1.2, 0.1, 0.5, 1.2",
    "1.2, 0.1, 1.0, 1.3",
    "1.2, 0.1, -1.0, 1.1",
    "1.2, 0.1, 2.0, 1.3",
    "2.0, 0.5, 1.0, 2.0",
    "0.5, 0.5, 0.0, 0.5"
  })
  @DisplayName("each use shifts the pitch within its spread, and never past what the game plays")
  void shiftsPitch(final float pitch, final float spread, final float roll, final float expected) {
    final PowerSound sound =
        new PowerSound(Identifier.ofVanilla("block.bell.use"), 1.0F, pitch, spread);
    assertEquals(expected, sound.pitchFor(roll), 1.0E-6F);
  }
}
