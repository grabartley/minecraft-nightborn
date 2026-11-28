package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class MobActionTest {

  private static final Path ACTIONS =
      Path.of("src/main/resources/data/nightborn/nightborn/mob_action");

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"leap", "dash", "dodge", "holy_water", "crucifix"})
  @DisplayName("each shipped built-in action loads with uses for a fight")
  void shipped(final String action) throws IOException {
    final MobAction loaded =
        MobAction.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(Files.readString(ACTIONS.resolve(action + ".json"))))
            .getOrThrow();
    assertFalse(loaded.uses().isEmpty());
    assertTrue(loaded.uses().stream().allMatch(use -> use.intent().equals("fight")));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"uses\":[{\"intent\":\"fight\"}]}|1",
        "{\"uses\":[{\"intent\":\"fight\"},{\"intent\":\"retreat\"}]}|2",
        "{\"uses\":[]}|0"
      })
  @DisplayName("an action reads its uses")
  void reads(final String json, final int uses) {
    final MobAction action =
        MobAction.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(uses, action.uses().size());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"{}", "{\"weight\":1}"})
  @DisplayName("an action with no uses listed is refused")
  void needsUses(final String json) {
    assertTrue(MobAction.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }

  @ParameterizedTest(name = "{0} uses")
  @ValueSource(ints = {0, 1, 3})
  @DisplayName("an action keeps its own copy of its uses")
  void copiesUses(final int count) {
    final List<MobUse> uses =
        new ArrayList<>(Collections.nCopies(count, new MobUse("fight", "target", List.of(), 1.0F)));
    final MobAction action = new MobAction(uses);
    uses.add(new MobUse("retreat", "self", List.of(), 1.0F));
    assertEquals(count, action.uses().size());
  }
}
