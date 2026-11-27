package com.grahambartley.nightborn.formview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class FormViewTest {

  private static final Path VIEWS =
      Path.of("src/main/resources/data/nightborn/nightborn/form_view");

  private static DataResult<FormView> parse(final String json) {
    return FormView.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
  }

  private static FormView shipped(final String name) throws IOException {
    return parse(Files.readString(VIEWS.resolve(name + ".json"))).getOrThrow();
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"bat_form", "night_terror", "mist_form"})
  @DisplayName("every shipped view loads")
  void shippedLoads(final String name) throws IOException {
    assertTrue(shipped(name).fadeInTicks() > 0);
  }

  @ParameterizedTest(name = "{0} is drawn for {1}")
  @CsvSource({
    "bat_form, nightborn:bat_form",
    "night_terror, nightborn:winged_form",
    "mist_form, nightborn:mist_form"
  })
  @DisplayName("each shipped view names the form it is drawn for")
  void namesItsForm(final String name, final String form) throws IOException {
    assertEquals(Identifier.of(form), shipped(name).form());
  }

  @Test
  @DisplayName("the Night Terror view wins over bat form, since a Night Terror is also a bat")
  void nightTerrorOutranksBat() throws IOException {
    assertTrue(shipped("night_terror").priority() > shipped("bat_form").priority());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"bat_form, echo", "night_terror, tint", "night_terror, tremble", "mist_form, wash"})
  @DisplayName("each shipped view has the parts its form calls for")
  void hasItsParts(final String name, final String part) throws IOException {
    final FormView view = shipped(name);
    final Optional<?> found =
        switch (part) {
          case "echo" -> view.echo();
          case "tint" -> view.tint();
          case "tremble" -> view.tremble();
          case "wash" -> view.wash();
          default -> throw new IllegalArgumentException(part);
        };
    assertTrue(found.isPresent());
  }

  @Test
  @DisplayName("mist form's view comes and goes quickly")
  void mistIsQuick() throws IOException {
    final FormView mist = shipped("mist_form");
    final FormView bat = shipped("bat_form");
    assertTrue(mist.fadeInTicks() < bat.fadeInTicks());
    assertTrue(mist.fadeOutTicks() < bat.fadeOutTicks());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"form\":\"nightborn:x\",\"fade_in_ticks\":5,\"fade_out_ticks\":5}",
        "{\"form\":\"nightborn:x\",\"fade_in_ticks\":5,\"fade_out_ticks\":5,\"darkness\":0.0}"
      })
  @DisplayName("a view that changes nothing is refused and names its form")
  void refusesEmptyView(final String json) {
    final String message = parse(json).error().orElseThrow().message();
    assertTrue(message.contains("nightborn:x"), message);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"form\":\"nightborn:x\",\"fade_in_ticks\":0,\"fade_out_ticks\":5,\"darkness\":1.0}",
        "{\"form\":\"nightborn:x\",\"fade_in_ticks\":5,\"fade_out_ticks\":5,\"darkness\":1.5}",
        "{\"fade_in_ticks\":5,\"fade_out_ticks\":5,\"darkness\":1.0}",
        "{\"form\":\"nightborn:x\",\"fade_in_ticks\":5,\"fade_out_ticks\":5,"
            + "\"tint\":{\"color\":\"red\",\"centre\":0.1,\"edge\":0.5}}"
      })
  @DisplayName("a view with a missing or out-of-range number is refused")
  void refusesBadNumbers(final String json) {
    assertFalse(parse(json).isSuccess());
  }

  @Test
  @DisplayName("a darkness-only view is allowed and has no priority unless given one")
  void darknessOnly() {
    final FormView view =
        parse(
                "{\"form\":\"nightborn:x\",\"fade_in_ticks\":5,\"fade_out_ticks\":5,\"darkness\":0.5}")
            .getOrThrow();
    assertEquals(0, view.priority());
    assertEquals(0.5F, view.darkness());
  }
}
