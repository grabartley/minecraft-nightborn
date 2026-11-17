package com.grahambartley.nightborn.tools.structures.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VampireStructuresTest {

  @Test
  void buildsEveryVampirePieceOnce() {
    assertEquals(
        List.of(
            "vampire/crypt/undercroft",
            "vampire/crypt/ossuary",
            "vampire/crypt/church_tower",
            "vampire/crypt/church_bellcote",
            "vampire/lair/den",
            "vampire/lair/grotto",
            "vampire/castle/bastions",
            "vampire/castle/ruined",
            "vampire/manor/turret",
            "vampire/manor/tower",
            "vampire/mountain_castle/eyrie",
            "vampire/mountain_castle/bastion"),
        VampireStructures.build().stream().map(Template::path).toList());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "vampire/crypt/undercroft",
        "vampire/crypt/ossuary",
        "vampire/lair/den",
        "vampire/lair/grotto",
        "vampire/castle/bastions",
        "vampire/castle/ruined",
        "vampire/manor/turret",
        "vampire/manor/tower"
      })
  void everyVampireHomeHoldsOneLoadedGramophone(final String path) {
    final Template home =
        VampireStructures.build().stream()
            .filter(t -> t.path().equals(path))
            .findFirst()
            .orElseThrow();
    final List<Pos> gramophones =
        home.cells().entrySet().stream()
            .filter(cell -> cell.getValue().startsWith(Furnishing.GRAMOPHONE + "["))
            .map(Map.Entry::getKey)
            .toList();
    assertEquals(1, gramophones.size());
    assertTrue(home.blockData().get(gramophones.getFirst()).containsKey("RecordItem"));
    final Pos below = gramophones.getFirst();
    assertNotEquals(
        "minecraft:air", home.get(below.x(), below.y() - 1, below.z()), "it stands on something");
  }
}
