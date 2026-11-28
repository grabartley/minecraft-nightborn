package com.grahambartley.nightborn.mob.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.mob.brain.Disposition;
import com.grahambartley.nightborn.mob.brain.Intent;
import com.grahambartley.nightborn.mob.brain.Mind;
import com.grahambartley.nightborn.mob.brain.Sharpness;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StageCombatTest {

  @Test
  @DisplayName("a stage with no fighting habits fights like a plain vampire")
  void defaults() {
    assertEquals(
        StageCombat.PLAIN,
        StageCombat.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow());
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"blood_floor\": 20} | true",
        "{\"blood_floor\": 21} | false",
        "{\"think_ticks\": 0} | false",
        "{\"think_ticks\": 200} | true",
        "{\"leap_chance\": 101} | false",
        "{\"dash_chance\": -1} | false",
        "{\"dodge_chance\": 100} | true",
        "{\"power_dodge_chance\": 100} | true",
        "{\"power_dodge_chance\": 101} | false",
        "{\"power_dodge_chance\": -1} | false",
        "{\"retreat_health\": 101} | false",
        "{\"stuck\": {\"ticks\": 0}} | false",
        "{\"stuck\": {\"ticks\": 25}} | true",
        "{\"stuck\": {\"ticks\": 201}} | false"
      })
  @DisplayName(
      "blood floors stay within a blood bar, chances within 0 to 100 and waits within ten"
          + " seconds")
  void bounds(final String json, final boolean loads) {
    assertEquals(
        loads, StageCombat.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"0", "35", "100"})
  @DisplayName("a stage reads how often it dodges with a power")
  void powerDodgeChance(final int chance) {
    assertEquals(
        chance,
        StageCombat.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("{\"power_dodge_chance\": " + chance + "}"))
            .getOrThrow()
            .powerDodgeChance());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"1", "25", "200"})
  @DisplayName("a stage reads how long it stays stuck before it tries something else")
  void stuckTicks(final int ticks) {
    assertEquals(
        ticks,
        StageCombat.CODEC
            .parse(
                JsonOps.INSTANCE, JsonParser.parseString("{\"stuck\": {\"ticks\": " + ticks + "}}"))
            .getOrThrow()
            .stuck()
            .ticks());
  }

  private static final Path STAGES =
      Path.of("src/main/resources/data/nightborn/nightborn/vampire_stage");

  private static Sharpness shipped(final String stage) throws IOException {
    return StageCombat.CODEC
        .parse(
            JsonOps.INSTANCE,
            JsonParser.parseString(Files.readString(STAGES.resolve(stage + ".json")))
                .getAsJsonObject()
                .get("combat"))
        .getOrThrow()
        .sharpness();
  }

  @ParameterizedTest(name = "{0} is sharper than {1}")
  @CsvSource({"adult, fledgling", "elder, adult", "ancient, elder"})
  @DisplayName("an older stage blurs its choice less, sees more and plans further ahead")
  void olderStagesAreSharper(final String older, final String younger) throws IOException {
    final Sharpness olderSharpness = shipped(older);
    final Sharpness youngerSharpness = shipped(younger);
    assertTrue(olderSharpness.blur() < youngerSharpness.blur());
    assertTrue(olderSharpness.foresight() > youngerSharpness.foresight());
    assertTrue(olderSharpness.reads().containsAll(youngerSharpness.reads()));
  }

  @ParameterizedTest(name = "{0} reads group {1} and place {2}")
  @CsvSource({"fledgling, false, false", "ancient, true, true"})
  @DisplayName("a fledgling reads only itself and its foe, and an ancient reads the whole fight")
  void readsByStage(final String stage, final boolean group, final boolean place)
      throws IOException {
    assertEquals(group, shipped(stage).reads(Sharpness.Reads.GROUP));
    assertEquals(place, shipped(stage).reads(Sharpness.Reads.PLACE));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"lord", "eldest"})
  @DisplayName("the lord and the Eldest keep the keen mind they had")
  void leadersStayKeen(final String stage) throws IOException {
    assertEquals(Sharpness.KEEN, shipped(stage));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {"{\"sharpness\": {\"blur\": 0.3}} | true", "{\"sharpness\": {\"blur\": 2}} | false"})
  @DisplayName("a stage refuses a sharpness out of range")
  void sharpnessBounds(final String json, final boolean loads) {
    assertEquals(
        loads, StageCombat.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"fledgling", "ancient"})
  @DisplayName(
      "a stage's mind thinks at its pace and sharpness, leaning as its vampire is disposed")
  void mind(final String stage) throws IOException {
    final StageCombat combat =
        StageCombat.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(Files.readString(STAGES.resolve(stage + ".json")))
                    .getAsJsonObject()
                    .get("combat"))
            .getOrThrow();
    final Disposition hot = new Disposition(Map.of(Intent.FIGHT, 1.5F), Map.of(), 0.5F);
    assertEquals(new Mind(combat.thinkTicks(), combat.sharpness(), hot), combat.mind(hot));
  }
}
