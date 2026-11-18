package com.grahambartley.nightborn.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DeedCriterionTest {

  private static final Path ADVANCEMENTS = Path.of("src/main/resources/data/nightborn/advancement");

  private static DeedCriterion.Conditions read(final String json) {
    return DeedCriterion.Conditions.CODEC
        .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
        .getOrThrow();
  }

  private static Deed deed(final String kind) {
    return Deed.of(Identifier.of(kind));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{}",
        "{\"facts\":{\"stage\":\"adult\"}}",
        "{\"deed\":\"nightborn:staked\",\"at_least\":{\"n\":\"x\"}}"
      })
  void rejectsConditionsItCannotRead(final String json) {
    assertTrue(
        DeedCriterion.Conditions.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .isError());
  }

  static Stream<Arguments> deeds() {
    return Stream.of(
        Arguments.of(
            "{\"deed\":\"nightborn:finisher_kill\"}", deed("nightborn:finisher_kill"), true),
        Arguments.of("{\"deed\":\"nightborn:finisher_kill\"}", deed("nightborn:downed"), false),
        Arguments.of(
            "{\"deed\":\"nightborn:stage_reached\",\"facts\":{\"stage\":\"nightborn:adult\"}}",
            deed("nightborn:stage_reached").with("stage", "nightborn:adult"),
            true),
        Arguments.of(
            "{\"deed\":\"nightborn:stage_reached\",\"facts\":{\"stage\":\"nightborn:adult\"}}",
            deed("nightborn:stage_reached").with("stage", "nightborn:elder"),
            false),
        Arguments.of(
            "{\"deed\":\"nightborn:staff_kept\",\"at_least\":{\"kinds\":3}}",
            deed("nightborn:staff_kept").with("kinds", 2),
            false),
        Arguments.of(
            "{\"deed\":\"nightborn:staff_kept\",\"at_least\":{\"kinds\":3}}",
            deed("nightborn:staff_kept").with("kinds", 3),
            true));
  }

  @ParameterizedTest(name = "{0} on {1} -> {2}")
  @MethodSource("deeds")
  void firesOnlyForTheDeedItNames(final String json, final Deed done, final boolean expected) {
    assertEquals(expected, read(json).matches(done));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"deed\":\"nightborn:finisher_kill\"}",
        "{\"deed\":\"nightborn:vampire_staked\",\"facts\":{\"in_coffin\":\"true\"}}",
        "{\"deed\":\"nightborn:trial_completed\",\"at_least\":{\"distinct\":15}}"
      })
  void advancementJsonRoundTripsThroughTheCodec(final String json) {
    final JsonElement parsed = JsonParser.parseString(json);
    assertEquals(
        parsed,
        DeedCriterion.Conditions.CODEC.encodeStart(JsonOps.INSTANCE, read(json)).getOrThrow());
  }

  static Stream<Arguments> advancementDeeds() throws IOException {
    final List<Arguments> found = new ArrayList<>();
    try (Stream<Path> files = Files.list(ADVANCEMENTS)) {
      for (final Path file : files.filter(f -> f.toString().endsWith(".json")).sorted().toList()) {
        final JsonObject criteria =
            JsonParser.parseString(Files.readString(file))
                .getAsJsonObject()
                .getAsJsonObject("criteria");
        for (final String name : criteria.keySet()) {
          final JsonObject criterion = criteria.getAsJsonObject(name);
          if (criterion.get("trigger").getAsString().equals(DeedCriterion.ID.toString())) {
            found.add(
                Arguments.of(
                    file.getFileName() + "#" + name, criterion.getAsJsonObject("conditions")));
          }
        }
      }
    }
    return found.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("advancementDeeds")
  void everyShippedDeedConditionReadsAndNamesADeedTheGameReports(
      final String where, final JsonObject conditions) {
    assertTrue(
        DeedKinds.live().contains(read(conditions.toString()).match().deed()),
        where + " names a deed the game reports");
  }
}
