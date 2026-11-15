package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RigArmorLayerTest {

  private static final Path GEO = Path.of("src/client/resources/assets/nightborn/geo/entity");

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {"vampire_male", "vampire_female", "vampire_player_wide", "vampire_player_slim"})
  @DisplayName("every rig that wears clothing has an anchor bone for each armour piece")
  void everyRigHasEveryAnchor(final String rig) throws IOException {
    final Set<String> bones = new HashSet<>();
    JsonParser.parseString(Files.readString(GEO.resolve(rig + ".geo.json")))
        .getAsJsonObject()
        .getAsJsonArray("minecraft:geometry")
        .get(0)
        .getAsJsonObject()
        .getAsJsonArray("bones")
        .forEach(bone -> bones.add(bone.getAsJsonObject().get("name").getAsString()));
    assertTrue(bones.containsAll(RigArmorLayer.PIECES.keySet()), rig + " has " + bones);
  }
}
