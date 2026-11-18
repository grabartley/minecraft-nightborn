package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.house.crest.Crest;
import com.grahambartley.nightborn.house.crest.CrestCharge;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CrestDyesTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets");

  @ParameterizedTest(name = "{0}, {1} on {2}")
  @CsvSource({
    "RAVEN, PURPLE, YELLOW, 0xFF8932B8, 0xFFFED83D",
    "SKULL, BLACK, WHITE, 0xFF1D1D21, 0xFFF9FFFE"
  })
  @DisplayName(
      "the field takes the first colour, the bordure and charge the second, the outline none")
  void layers(
      final CrestCharge charge,
      final DyeColor field,
      final DyeColor tincture,
      final String fieldArgb,
      final String tinctureArgb) {
    final List<CrestDyes.Dye> dyes = CrestDyes.of(new Crest(charge, field, tincture));
    assertEquals(
        List.of(
            "nightborn:textures/block/crest_banner_field.png",
            "nightborn:textures/block/crest_banner_bordure.png",
            "nightborn:textures/block/crest_charge/" + charge.asString() + ".png",
            "nightborn:textures/block/crest_charge/" + charge.asString() + "_outline.png"),
        dyes.stream().map(dye -> dye.texture().toString()).toList());
    final int fieldColour = Integer.parseUnsignedInt(fieldArgb.substring(2), 16);
    final int tinctureColour = Integer.parseUnsignedInt(tinctureArgb.substring(2), 16);
    assertEquals(
        List.of(fieldColour, tinctureColour, tinctureColour, CrestDyes.UNTINTED),
        dyes.stream().map(CrestDyes.Dye::colour).toList());
    dyes.forEach(
        dye ->
            assertTrue(
                Files.isRegularFile(
                    ASSETS
                        .resolve(dye.texture().getNamespace())
                        .resolve(dye.texture().getPath()))));
  }
}
