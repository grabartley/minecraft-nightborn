package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class VampireLookTest {

  @ParameterizedTest(name = "look {0}: {1} rig, costume {2}")
  @CsvSource({
    "0, MALE, 0, male, male_0",
    "2, MALE, 2, male, male_2",
    "3, FEMALE, 0, female, female_0",
    "5, FEMALE, 2, female, female_2",
    "6, MALE, 0, male, male_0",
    "-1, FEMALE, 2, female, female_2"
  })
  @DisplayName("each look picks a rig and a costume texture made for it")
  void look(
      final int index,
      final VampireLook.Rig rig,
      final int costume,
      final String geo,
      final String texture) {
    final VampireLook look = VampireLook.of(index);
    assertEquals(rig, look.rig());
    assertEquals(costume, look.costume());
    assertEquals("nightborn:geo/entity/vampire_" + geo + ".geo.json", look.geo().toString());
    assertEquals(
        "nightborn:textures/entity/vampire/" + texture + ".png", look.texture().toString());
    assertEquals("nightborn:textures/entity/vampire/" + geo + "_eyes.png", look.eyes().toString());
  }

  @ParameterizedTest(name = "{0} wraps to {1}")
  @CsvSource({"-4, 2", "-1, 5", "0, 0", "5, 5", "6, 0", "99, 3"})
  @DisplayName("a saved look outside the range wraps to a real one")
  void wraps(final int given, final int kept) {
    assertEquals(kept, VampireLook.of(given).index());
  }

  static Stream<Integer> looks() {
    return IntStream.range(0, VampireLook.COUNT).boxed();
  }

  @ParameterizedTest(name = "look {0}")
  @MethodSource("looks")
  @DisplayName("every look's rig, costume and eye layer ship with the mod")
  void artShips(final int index) {
    final VampireLook look = VampireLook.of(index);
    assertTrue(Files.exists(asset(CLIENT, look.geo())), look.geo().toString());
    assertTrue(Files.exists(asset(MAIN, look.texture())), look.texture().toString());
    assertTrue(Files.exists(asset(MAIN, look.eyes())), look.eyes().toString());
    assertTrue(Files.exists(asset(CLIENT, VampireLook.animation())));
  }

  private static final Path CLIENT = Path.of("src/client/resources/assets");
  private static final Path MAIN = Path.of("src/main/resources/assets");

  private static Path asset(final Path root, final Identifier id) {
    return root.resolve(id.getNamespace()).resolve(id.getPath());
  }
}
