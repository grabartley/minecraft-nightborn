package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessoriesTest {

  private static final Path DATA = Path.of("src/main/resources/data/nightborn/nightborn/accessory");

  @Test
  @DisplayName("accessories are a data registry of their own")
  void registryKey() {
    assertEquals("nightborn:accessory", Accessories.KEY.getValue().toString());
  }

  static java.util.stream.Stream<String> ids() {
    return AccessoryItems.IDS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("ids")
  @DisplayName("every piece has a data file that reads cleanly")
  void eachPieceHasData(final String piece) throws IOException {
    final Path file = DATA.resolve(piece + ".json");
    assertTrue(Files.exists(file), file.toString());
    assertTrue(
        Accessory.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file)))
            .isSuccess());
  }
}
