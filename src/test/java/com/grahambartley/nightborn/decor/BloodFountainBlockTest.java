package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BloodFountainBlockTest {

  private static final Path CLIENT = Path.of("src/client/resources/assets/nightborn");

  @ParameterizedTest(name = "powered={0} flowing={1}")
  @CsvSource({"false, true", "true, false"})
  void aRedstoneSignalStopsTheFlow(final boolean powered, final boolean flowing) {
    assertEquals(flowing, BloodFountainBlock.flowsWhen(powered));
  }

  @Test
  void flowsWhenFirstPlaced() {
    assertTrue(DecorBlocks.BLOOD_FOUNTAIN.getDefaultState().get(BloodFountainBlock.FLOWING));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "geo/block/blood_fountain.geo.json",
    "animations/block/blood_fountain.animation.json"
  })
  void drawsItsOwnRig(final String path) {
    final BloodFountainBlock fountain = (BloodFountainBlock) DecorBlocks.BLOOD_FOUNTAIN;
    assertEquals(BloodFountainBlock.FLOWING, fountain.toggleProperty());
    assertTrue(path.contains(fountain.artModel()));
    assertTrue(Files.exists(CLIENT.resolve(path)));
  }
}
