package com.grahambartley.nightborn.mob.look;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VampireOutfitTest {

  @Test
  @DisplayName("an outfit reads its pieces, each with an optional dye colour")
  void reads() {
    final VampireOutfit outfit =
        VampireOutfit.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"rig\":\"male\",\"era\":\"nightborn:georgian\",\"pieces\":["
                        + "{\"item\":\"nightborn:frock_coat\",\"color\":\"#5A1A24\"},"
                        + "{\"item\":\"nightborn:buckled_shoes\"}]}"))
            .getOrThrow();
    assertEquals(VampireRig.MALE, outfit.rig());
    assertEquals(1, outfit.weight());
    assertEquals(Optional.of(0x5A1A24), outfit.pieces().get(0).color());
    assertEquals(Optional.empty(), outfit.pieces().get(1).color());
  }

  @Test
  @DisplayName("a piece with a colour that is not #RRGGBB is rejected")
  void rejectsBadColour() {
    assertFalse(
        VampireOutfit.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"rig\":\"male\",\"era\":\"a:b\",\"pieces\":"
                        + "[{\"item\":\"a:b\",\"color\":\"red\"}]}"))
            .isSuccess());
  }

  @ParameterizedTest(name = "{0} outfit for {1} on a {2} look of {3}: {4}")
  @CsvSource({
    "MALE, georgian, MALE, georgian, true",
    "MALE, georgian, MALE, victorian, false",
    "MALE, georgian, FEMALE, georgian, false",
    "FEMALE, victorian, FEMALE, victorian, true"
  })
  @DisplayName("an outfit fits a look of the same rig turned in one of its eras")
  void fits(
      final VampireRig outfitRig,
      final String era,
      final VampireRig lookRig,
      final String lookEra,
      final boolean fits) {
    final VampireOutfit outfit = new VampireOutfit(outfitRig, id(era), List.of(), 1);
    final VampireLook look = new VampireLook(lookRig, id("t.png"), List.of(id(lookEra)), 1);
    assertEquals(fits, outfit.fits(look));
  }

  private static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }
}
