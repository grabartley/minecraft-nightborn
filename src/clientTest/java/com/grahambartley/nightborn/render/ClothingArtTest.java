package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.clothing.ClothingCut;
import com.grahambartley.nightborn.clothing.ClothingPiece;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClothingArtTest {

  @ParameterizedTest(name = "{0} {1}")
  @CsvSource({
    "FROCK_COAT, MALE, frock_coat_male",
    "FROCK_COAT, FEMALE, frock_coat_female",
    "RIDING_BOOTS, FEMALE, riding_boots_female"
  })
  @DisplayName("each piece has a model and texture per cut and one set of clips")
  void paths(final ClothingPiece piece, final ClothingCut cut, final String art) {
    assertEquals(
        Identifier.of("nightborn:geo/armor/" + art + ".geo.json"),
        ClothingArt.geo(piece.id(), cut));
    assertEquals(
        Identifier.of("nightborn:textures/armor/" + art + ".png"),
        ClothingArt.texture(piece.id(), cut));
    assertEquals(
        Identifier.of("nightborn:animations/armor/" + piece.id() + ".animation.json"),
        ClothingArt.animation(piece.id()));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({"dyed_cloth, true", "dyedSleeve, true", "trim, false", "armorBody, false"})
  @DisplayName("only bones named for dye take the wearer's colour")
  void dyedBones(final String bone, final boolean dyed) {
    assertEquals(dyed, ClothingArt.isDyed(bone));
  }
}
