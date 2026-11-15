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

  private static Path asset(final String root, final Identifier id) {
    return Path.of(root).resolve(id.getNamespace()).resolve(id.getPath());
  }
}
