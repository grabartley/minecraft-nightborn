package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ConfigJsonTest {

  @Test
  @DisplayName("an empty object reads as defaults with no problems")
  void emptyIsDefaults() {
    final ConfigFormat.Read<NightbornConfig> read = ConfigJson.read(new JsonObject());
    assertEquals(NightbornConfig.DEFAULTS, read.value());
    assertTrue(read.problems().isEmpty());
  }

  @Test
  @DisplayName("written settings read back unchanged")
  void roundTrips() {
    final NightbornConfig config =
        NightbornConfig.DEFAULTS
            .with(FeedingSettings.BOTTLE_SIZE, 9)
            .with(TurningSettings.PLAYERS_CAN_TURN_PLAYERS, false)
            .with(BloodSettings.PLAYER_BLOOD, 400);
    assertEquals(config, ConfigJson.read(ConfigJson.write(config)).value());
  }

  @Test
  @DisplayName("settings are written grouped by section")
  void groupsBySection() {
    final JsonObject json = ConfigJson.write(NightbornConfig.DEFAULTS);
    assertEquals(30, json.getAsJsonObject("downed").get("downedSeconds").getAsInt());
    assertEquals(false, json.getAsJsonObject("factions").get("showInPlayerList").getAsBoolean());
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"999, 600", "-3, 5", "2, 5", "600, 600"})
  @DisplayName("out of range numbers are clamped and reported")
  void clampsOutOfRange(final long written, final int loaded) {
    final ConfigFormat.Read<NightbornConfig> read =
        read("{\"downed\":{\"escapeSeconds\":" + written + "}}");
    assertEquals(loaded, read.value().downed().escapeSeconds());
    assertEquals(written >= 5 && written <= 600, read.problems().isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {"\"thirty\"", "true", "1.5", "{}", "[1]"})
  @DisplayName("a wrong type for a number falls back to its default and is reported")
  void wrongNumberType(final String value) {
    final ConfigFormat.Read<NightbornConfig> read =
        read("{\"downed\":{\"downedSeconds\":" + value + "}}");
    assertEquals(30, read.value().downed().downedSeconds());
    assertEquals(1, read.problems().size());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {"\"yes\"", "1", "null"})
  @DisplayName("a wrong type for a toggle falls back to its default and is reported")
  void wrongToggleType(final String value) {
    final ConfigFormat.Read<NightbornConfig> read =
        read("{\"downed\":{\"hardcoreVampireDeath\":" + value + "}}");
    assertEquals(false, read.value().downed().hardcoreVampireDeath());
    assertEquals(1, read.problems().size());
  }

  @Test
  @DisplayName("a section that is not an object is ignored")
  void badSectionIgnored() {
    assertEquals(NightbornConfig.DEFAULTS, read("{\"sun\":5}").value());
  }

  private static ConfigFormat.Read<NightbornConfig> read(final String json) {
    return ConfigJson.read(JsonParser.parseString(json).getAsJsonObject());
  }
}
