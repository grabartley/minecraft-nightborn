package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PictureBlockTest {

  private static final Path TEXTURES =
      Path.of("src/main/resources/assets/nightborn/textures/block");

  static Stream<Arguments> pictures() {
    return Stream.of(DecorBlocks.WANTED_POSTER, DecorBlocks.PORTRAIT)
        .flatMap(
            block ->
                IntStream.range(0, PictureBlock.PORTRAITS).mapToObj(n -> Arguments.of(block, n)));
  }

  @ParameterizedTest(name = "{0} picture {1}")
  @MethodSource("pictures")
  void everyPictureHasItsOwnTexture(final Block block, final int picture) {
    final String id = Registries.BLOCK.getId(block).getPath();
    assertTrue(block.getDefaultState().with(PictureBlock.PORTRAIT, picture) != null);
    assertTrue(Files.exists(TEXTURES.resolve(id + "_" + picture + ".png")), id + " " + picture);
  }

  @ParameterizedTest(name = "{0} pictures")
  @ValueSource(ints = {3})
  void offersThreePictures(final int pictures) {
    assertEquals(pictures, PictureBlock.PORTRAITS);
    assertEquals(pictures, PictureBlock.PORTRAIT.getValues().size());
  }
}
