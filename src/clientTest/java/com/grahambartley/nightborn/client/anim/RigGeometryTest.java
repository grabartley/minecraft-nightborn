package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RigGeometryTest {

  static RigGeometry rig(final String look) throws IOException {
    return RigGeometry.read(
        JsonParser.parseString(
                Files.readString(
                    Path.of(
                        "src/client/resources/assets/nightborn/geo/entity/vampire_"
                            + look
                            + ".geo.json")))
            .getAsJsonObject());
  }

  @ParameterizedTest(name = "{0} {1}: parent {2}")
  @CsvSource({
    "male, body, root",
    "male, head, body",
    "male, right_arm, body",
    "male, left_leg, root",
    "female, head, body",
    "female, right_leg, root"
  })
  @DisplayName("the shipped rigs hang the head and arms from the body, and the legs from the root")
  void hierarchy(final String look, final String bone, final String parent) throws IOException {
    assertEquals(parent, rig(look).bone(bone).orElseThrow().parent().orElseThrow());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"male", "female"})
  @DisplayName("both rigs bend at the hips and carry a mouth on the head")
  void hipsAndMouth(final String look) throws IOException {
    final RigGeometry rig = rig(look);
    assertEquals(new Vec3d(0, 12, 0), rig.bone("body").orElseThrow().pivot());
    assertEquals("head", rig.locator("mouth").orElseThrow().bone());
    assertTrue(rig.bone("root").orElseThrow().parent().isEmpty());
  }
}
