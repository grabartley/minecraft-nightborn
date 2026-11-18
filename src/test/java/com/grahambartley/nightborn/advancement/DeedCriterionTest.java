package com.grahambartley.nightborn.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.accessory.AccessorySlot;
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
import org.junit.jupiter.params.provider.CsvSource;
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

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"dressed_to_the_nines.json"})
  void everySlotMeansEveryAccessorySlot(final String file) throws IOException {
    final int slots =
        JsonParser.parseString(Files.readString(ADVANCEMENTS.resolve(file)))
            .getAsJsonObject()
            .getAsJsonObject("criteria")
            .getAsJsonObject("every_slot")
            .getAsJsonObject("conditions")
            .getAsJsonObject("at_least")
            .get("slots")
            .getAsInt();
    assertEquals(AccessorySlot.values().length, slots);
  }

  private static JsonObject criteria(final String file) throws IOException {
    return JsonParser.parseString(Files.readString(ADVANCEMENTS.resolve(file)))
        .getAsJsonObject()
        .getAsJsonObject("criteria");
  }

  private static List<String> fact(final JsonObject criteria, final String key) {
    return criteria.keySet().stream()
        .map(name -> criteria.getAsJsonObject(name).getAsJsonObject("conditions"))
        .map(conditions -> conditions.getAsJsonObject("facts").get(key).getAsString())
        .sorted()
        .toList();
  }

  private static List<String> ids(final Path folder, final String type) throws IOException {
    try (Stream<Path> files = Files.list(folder)) {
      final List<String> found = new ArrayList<>();
      for (final Path file : files.sorted().toList()) {
        final JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        if (type == null || type.equals(json.get("type").getAsString())) {
          found.add("nightborn:" + file.getFileName().toString().replace(".json", ""));
        }
      }
      return found;
    }
  }

  @ParameterizedTest(name = "{0} covers {2}")
  @CsvSource({
    "blood_type.json, gift, src/main/resources/data/nightborn/nightborn/blood_gift, ''",
    "property_ladder.json, structure, src/main/resources/data/nightborn/worldgen/structure,"
        + " nightborn:vampire_structure"
  })
  void aSetChallengeCoversExactlyTheSetInData(
      final String file, final String key, final String folder, final String type)
      throws IOException {
    assertEquals(ids(Path.of(folder), type.isEmpty() ? null : type), fact(criteria(file), key));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"ring_leader.json"})
  void everyRingInRingLeaderIsAnAccessory(final String file) throws IOException {
    final JsonObject criteria = criteria(file);
    for (final String name : criteria.keySet()) {
      for (final String ring :
          criteria
              .getAsJsonObject(name)
              .getAsJsonObject("conditions")
              .getAsJsonObject("facts")
              .keySet()) {
        assertTrue(
            Files.exists(
                Path.of(
                    "src/main/resources/data/nightborn/nightborn/accessory",
                    Identifier.of(ring).getPath() + ".json")),
            name + " names " + ring);
      }
    }
  }
}
