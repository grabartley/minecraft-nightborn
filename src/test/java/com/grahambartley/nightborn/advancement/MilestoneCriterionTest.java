package com.grahambartley.nightborn.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.LootContextPredicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class MilestoneCriterionTest {

  private static final LootContextPredicate ANY_ENTITY = LootContextPredicate.create();

  private static MilestoneCriterion.Conditions wanting(final Milestone milestone) {
    return new MilestoneCriterion.Conditions(
        Optional.empty(), milestone, Optional.empty(), NumberRange.IntRange.ANY);
  }

  @Test
  void isRegisteredAsNightbornMilestone() {
    assertEquals("nightborn:milestone", MilestoneCriterion.ID.toString());
  }

  static Stream<Arguments> milestonePairs() {
    return Arrays.stream(Milestone.values())
        .flatMap(
            wanted ->
                Arrays.stream(Milestone.values())
                    .map(reached -> Arguments.of(wanted, reached, wanted == reached)));
  }

  @ParameterizedTest(name = "wants {0}, reached {1} -> {2}")
  @MethodSource("milestonePairs")
  void matchesOnlyItsOwnMilestone(
      final Milestone wanted, final Milestone reached, final boolean expected) {
    assertEquals(expected, wanting(wanted).matches(reached, 1, predicate -> true));
  }

  @ParameterizedTest(name = "at least {0}, reached {1} -> {2}")
  @CsvSource({
    "3, 0, false",
    "3, 2, false",
    "3, 3, true",
    "3, 7, true",
    "2, 1, false",
    "2, 2, true"
  })
  void aCountNeedsAtLeastItsMinimum(final int min, final int reached, final boolean expected) {
    final MilestoneCriterion.Conditions conditions =
        new MilestoneCriterion.Conditions(
            Optional.empty(),
            Milestone.GARLIC_STRINGS,
            Optional.empty(),
            NumberRange.IntRange.atLeast(min));
    assertEquals(expected, conditions.matches(Milestone.GARLIC_STRINGS, reached, p -> true));
  }

  @ParameterizedTest(name = "other entity matches: {0}")
  @ValueSource(booleans = {true, false})
  void anEntityConditionFollowsTheOtherEntity(final boolean otherMatches) {
    final MilestoneCriterion.Conditions conditions =
        new MilestoneCriterion.Conditions(
            Optional.empty(), Milestone.FED, Optional.of(ANY_ENTITY), NumberRange.IntRange.ANY);
    assertEquals(otherMatches, conditions.matches(Milestone.FED, 1, p -> otherMatches));
  }

  @ParameterizedTest(name = "other entity matches: {0}")
  @ValueSource(booleans = {true, false})
  void withoutAnEntityConditionTheOtherEntityIsIgnored(final boolean otherMatches) {
    assertEquals(true, wanting(Milestone.FED).matches(Milestone.FED, 1, p -> otherMatches));
  }

  static List<String> conditionJson() {
    return List.of(
        "{\"milestone\":\"turned\"}",
        "{\"milestone\":\"garlic_strings\",\"count\":{\"min\":3}}",
        "{\"milestone\":\"bloodlines\",\"count\":{\"min\":2}}");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("conditionJson")
  void advancementJsonRoundTripsThroughTheCodec(final String text) {
    final JsonObject json = JsonParser.parseString(text).getAsJsonObject();
    final MilestoneCriterion.Conditions parsed =
        MilestoneCriterion.Conditions.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
    assertEquals(json.get("milestone").getAsString(), parsed.milestone().asString());
    assertEquals(
        parsed,
        MilestoneCriterion.Conditions.CODEC
            .parse(
                JsonOps.INSTANCE,
                MilestoneCriterion.Conditions.CODEC
                    .encodeStart(JsonOps.INSTANCE, parsed)
                    .getOrThrow())
            .getOrThrow());
  }
}
