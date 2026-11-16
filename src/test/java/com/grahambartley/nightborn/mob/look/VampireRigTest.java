package com.grahambartley.nightborn.mob.look;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonPrimitive;
import com.grahambartley.nightborn.clothing.ClothingCut;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class VampireRigTest {

  @ParameterizedTest(name = "{0} reads as {1} and wears the {2} cut")
  @CsvSource({"male, MALE, MALE", "female, FEMALE, FEMALE"})
  @DisplayName("a rig reads from data and fits clothing of its own cut")
  void readsAndFits(final String id, final VampireRig rig, final ClothingCut cut) {
    assertEquals(rig, VampireRig.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(id)).getOrThrow());
    assertEquals(cut, rig.cut());
    assertEquals(id, rig.asString());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(VampireRig.class)
  @DisplayName("every rig's model, eye glow and the shared clips ship with the mod")
  void artShips(final VampireRig rig) {
    assertTrue(Files.exists(asset("src/client/resources/assets", rig.geo())), rig.geo().toString());
    assertTrue(Files.exists(asset("src/main/resources/assets", rig.eyes())), rig.eyes().toString());
    assertTrue(Files.exists(asset("src/client/resources/assets", VampireRig.animation())));
  }

  @ParameterizedTest(name = "{0} rolled {1} is {2}")
  @CsvSource({
    "FEMALE, 0.0, 0.7",
    "FEMALE, 0.5, 1.0",
    "FEMALE, 1.0, 1.3",
    "FEMALE, -1.0, 0.7",
    "FEMALE, 2.0, 1.3",
    "MALE, 0.0, 1.0",
    "MALE, 1.0, 1.0"
  })
  @DisplayName("each body rolls its bust between its rig's smallest and largest")
  void rollsBustWithinRange(final VampireRig rig, final double roll, final float bust) {
    assertEquals(bust, rig.bust(roll), 1.0E-6F);
  }

  @ParameterizedTest(name = "{0} middle is {1}")
  @CsvSource({"FEMALE, 1.0", "MALE, 1.0"})
  @DisplayName("each rig's middle bust sits halfway between its smallest and largest")
  void middleBustIsHalfway(final VampireRig rig, final float middle) {
    assertEquals(middle, rig.middleBust(), 1.0E-6F);
  }

  @ParameterizedTest(name = "{0} {1} fits to {2}")
  @CsvSource({"FEMALE, 0.1, 0.7", "FEMALE, 1.1, 1.1", "FEMALE, 9.0, 1.3", "MALE, 1.2, 1.0"})
  @DisplayName("a bust outside the rig's range is brought back into it")
  void fitsBustToRange(final VampireRig rig, final float bust, final float fitted) {
    assertEquals(fitted, rig.fitBust(bust), 1.0E-6F);
  }

  private static Path asset(final String root, final Identifier id) {
    return Path.of(root).resolve(id.getNamespace()).resolve(id.getPath());
  }
}
