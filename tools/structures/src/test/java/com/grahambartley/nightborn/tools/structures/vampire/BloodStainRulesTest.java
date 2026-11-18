package com.grahambartley.nightborn.tools.structures.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class BloodStainRulesTest {

  private static final JsonObject DATA =
      JsonParser.parseString(
              """
              {"default": {"clusters": 3, "radius": 2, "floor_splatters": 5, "pools": 1,
                           "wall_splatters": 2, "wall_max_height": 1},
               "structures": {"vampire/castle": {"clusters": 6},
                              "vampire/castle/ruined": {"pools": 0}}}
              """)
          .getAsJsonObject();

  @ParameterizedTest(name = "{0} takes {1} clusters and {2} pools")
  @CsvSource({
    "vampire/lair/den, 3, 1",
    "vampire/castle/bastions, 6, 1",
    "vampire/castle/ruined, 3, 0"
  })
  void theLongestMatchingPathOverridesTheDefault(
      final String path, final int clusters, final int pools) {
    final BloodStainRules rules = BloodStainRules.forPath(DATA, path);
    assertEquals(clusters, rules.clusters());
    assertEquals(pools, rules.pools());
    assertEquals(2, rules.radius());
    assertEquals(1, rules.wallMaxHeight());
  }

  @ParameterizedTest(name = "the shipped rules stain {0}")
  @ValueSource(
      strings = {
        "vampire/crypt/undercroft",
        "vampire/crypt/church_tower",
        "vampire/lair/grotto",
        "vampire/castle/ruined",
        "vampire/manor/turret",
        "vampire/mountain_castle/eyrie"
      })
  void theShippedRulesStainEveryKind(final String path) {
    final BloodStainRules rules = BloodStainRules.forPath(path);
    assertTrue(rules.clusters() >= 1);
    assertTrue(rules.floorSplatters() >= 1);
    assertTrue(rules.wallMaxHeight() <= 1, "wall stains stay low");
  }
}
