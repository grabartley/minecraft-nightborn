package com.grahambartley.nightborn.client.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.config.ConfigFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ClientStateJsonTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(GoreLevel.class)
  @DisplayName("a written gore level reads back unchanged")
  void roundTrips(final GoreLevel level) {
    final ClientState state = ClientState.DEFAULTS.withGore(level);
    final ConfigFormat.Read<ClientState> read = ClientStateJson.read(ClientStateJson.write(state));
    assertEquals(state, read.value());
    assertTrue(read.problems().isEmpty());
  }

  @Test
  @DisplayName("an empty file reads as defaults with no problems")
  void emptyIsDefaults() {
    final ConfigFormat.Read<ClientState> read = read("{}");
    assertEquals(ClientState.DEFAULTS, read.value());
    assertTrue(read.problems().isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"\"gory\"", "\"FULL\"", "2", "true", "null", "[\"off\"]", "{}"})
  @DisplayName("a gore value that is not a known level falls back to full and is reported")
  void badValueFallsBack(final String value) {
    final ConfigFormat.Read<ClientState> read = read("{\"gore\":" + value + "}");
    assertEquals(GoreLevel.FULL, read.value().gore());
    assertEquals(1, read.problems().size());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(FormViewLevel.class)
  @DisplayName("a written form view level reads back unchanged")
  void formViewsRoundTrip(final FormViewLevel level) {
    final ClientState state = ClientState.DEFAULTS.withFormViews(level);
    final ConfigFormat.Read<ClientState> read = ClientStateJson.read(ClientStateJson.write(state));
    assertEquals(state, read.value());
    assertTrue(read.problems().isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"\"dim\"", "\"OFF\"", "0", "false", "null", "[\"full\"]", "{}"})
  @DisplayName("a form view value that is not a known level falls back to full and is reported")
  void badFormViewsFallsBack(final String value) {
    final ConfigFormat.Read<ClientState> read = read("{\"formViews\":" + value + "}");
    assertEquals(FormViewLevel.FULL, read.value().formViews());
    assertEquals(1, read.problems().size());
    assertTrue(read.problems().get(0).startsWith("formViews is not one of full, reduced, off"));
  }

  @ParameterizedTest(name = "skip {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("a written ceremony camera choice reads back unchanged")
  void skipRoundTrips(final boolean skip) {
    final ClientState state = ClientState.DEFAULTS.withSkipCeremonyCamera(skip);
    assertEquals(state, ClientStateJson.read(ClientStateJson.write(state)).value());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"\"yes\"", "1", "null", "[true]", "{}"})
  @DisplayName("a ceremony camera value that is not true or false shows the camera and is reported")
  void badSkipFallsBack(final String value) {
    final ConfigFormat.Read<ClientState> read = read("{\"skipCeremonyCamera\":" + value + "}");
    assertEquals(false, read.value().skipCeremonyCamera());
    assertEquals(1, read.problems().size());
  }

  private static ConfigFormat.Read<ClientState> read(final String json) {
    return ClientStateJson.read(JsonParser.parseString(json).getAsJsonObject());
  }
}
