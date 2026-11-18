package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AccessoryPieceTest {

  @ParameterizedTest(name = "{0}: dyeable {2}, drawn {3}")
  @CsvSource({
    "MOURNING_VEIL, mourning_veil, true, true",
    "HIGH_COLLARED_CAPE, high_collared_cape, true, true",
    "ONYX_RING, onyx_ring, false, false",
    "VELVET_CHOKER, velvet_choker, false, true",
    "CAMEO_BROOCH, cameo_brooch, false, true",
    "LACE_GLOVES, lace_gloves, false, true",
    "BRASS_BUCKLED_BELT, brass_buckled_belt, false, true"
  })
  @DisplayName("each piece has its id, whether it takes dye, and every piece but a ring is drawn")
  void facts(
      final AccessoryPiece piece, final String id, final boolean dyeable, final boolean drawn) {
    assertEquals(id, piece.id());
    assertEquals(dyeable, piece.dyeable());
    assertEquals(drawn, piece.drawn());
  }

  @ParameterizedTest(name = "{0}: #{1}")
  @CsvSource({"MOURNING_VEIL, 1A1719", "HIGH_COLLARED_CAPE, 5A1A24"})
  @DisplayName("the veil and the cape keep the colour they had as clothing")
  void keepsItsColour(final AccessoryPiece piece, final String colour) {
    assertEquals(Integer.parseInt(colour, 16), piece.undyedColor());
  }
}
