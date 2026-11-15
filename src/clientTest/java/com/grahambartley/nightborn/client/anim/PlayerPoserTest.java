package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Map;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlayerPoserTest {

  private static PlayerEntityModel<PlayerEntity> model() {
    return new PlayerEntityModel<>(RigSpaceTest.vanillaPlayer(), false);
  }

  private static RigPose bend(final double degrees) {
    return new RigPose(Map.of("body", new BonePose(new Vec3d(degrees, 0, 0), Vec3d.ZERO)));
  }

  @Test
  @DisplayName("a rest pose leaves the model untouched")
  void restIsUntouched() throws IOException {
    final PlayerEntityModel<PlayerEntity> model = model();
    PlayerPoser.pose(model, RigPose.REST, RigGeometryTest.rig("male"));
    assertEquals(0.0F, model.head.pivotY);
    assertEquals(0.0F, model.body.pitch);
  }

  @ParameterizedTest(name = "bend {0}")
  @CsvSource({"20", "45"})
  @DisplayName("bending at the hips carries the head and arms with the body, and leaves the legs")
  void bendCarriesChildren(final double degrees) throws IOException {
    final PlayerEntityModel<PlayerEntity> model = model();
    PlayerPoser.pose(model, bend(degrees), RigGeometryTest.rig("male"));
    assertEquals((float) Math.toRadians(degrees), model.body.pitch, 1.0E-4F);
    assertEquals((float) Math.toRadians(degrees), model.head.pitch, 1.0E-4F);
    assertTrue(model.head.pivotZ < 0.0F, "the head goes forward");
    assertTrue(model.head.pivotY > 0.0F, "and down");
    assertTrue(model.rightArm.pivotZ < 0.0F, "the arms go with it");
    assertEquals(0.0F, model.rightLeg.pitch);
    assertEquals(12.0F, model.rightLeg.pivotY);
  }

  @Test
  @DisplayName("the hat layer follows the head")
  void hatFollowsHead() throws IOException {
    final PlayerEntityModel<PlayerEntity> model = model();
    PlayerPoser.pose(model, bend(30), RigGeometryTest.rig("male"));
    assertEquals(model.head.pivotZ, model.hat.pivotZ);
    assertEquals(model.head.pitch, model.hat.pitch);
  }
}
