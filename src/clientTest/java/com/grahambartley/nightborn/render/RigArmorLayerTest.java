package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RigArmorLayerTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"vampire_male", "vampire_female", "player_wide", "player_slim"})
  @DisplayName("every rig that wears clothing has an anchor bone for each armour piece")
  void everyRigHasEveryAnchor(final String rig) throws IOException {
    final Set<String> bones = GeoAssets.boneNames(GeoAssets.ENTITY_GEO.resolve(rig + ".geo.json"));
    assertTrue(bones.containsAll(RigArmorLayer.PIECES.keySet()), rig + " has " + bones);
  }
}
