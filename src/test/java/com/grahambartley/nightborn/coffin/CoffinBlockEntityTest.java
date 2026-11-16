package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CoffinBlockEntityTest {

  private static final double TICKS_PER_SECOND = 20.0;

  @ParameterizedTest(name = "found open {0}, then open {1} for {2} ticks: {3}")
  @CsvSource({
    "false, false, 3, 0.0",
    "true, true, 3, 1.0",
    "false, true, 4, 0.4",
    "true, false, 2, 0.8"
  })
  @DisplayName("the lid follows the block's open state a tenth of the way each tick")
  void followsTheBlockState(
      final boolean found, final boolean then, final int ticks, final float openness) {
    final BlockState head =
        Coffins.get(CoffinMaterial.DARK_OAK).getDefaultState().with(BedBlock.PART, BedPart.HEAD);
    final CoffinBlockEntity coffin = new CoffinBlockEntity(BlockPos.ORIGIN, head);
    coffin.clientTick(head.with(CoffinBlock.OPEN, found));
    for (int i = 0; i < ticks; i++) {
      coffin.clientTick(head.with(CoffinBlock.OPEN, then));
    }
    assertEquals(openness, coffin.openness(1.0F), 1.0E-5F);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"WOOD, open", "WOOD, close", "STONE, open", "STONE, close"})
  @DisplayName(
      "each lid clip lasts as long as the lid takes to move, so a sleeper vanishes as it shuts")
  void lidClipsMatchTheLidTimer(final CoffinStyle style, final String clip) throws IOException {
    final JsonObject animations =
        JsonParser.parseString(
                Files.readString(
                    Path.of(
                        "src/client/resources/assets/nightborn/animations/block/"
                            + style.model()
                            + ".animation.json")))
            .getAsJsonObject()
            .getAsJsonObject("animations");
    assertEquals(CoffinBlockEntity.LID_CLIPS.all(), animations.keySet());
    final double ticks = 1.0 / LidMotion.STEP;
    assertEquals(
        ticks / TICKS_PER_SECOND,
        animations.getAsJsonObject(clip).get("animation_length").getAsDouble(),
        1.0E-6);
  }
}
