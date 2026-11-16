package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import net.minecraft.util.math.MathHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;

class HeadLookTest {

  private static final int FRAMES = 200;
  private static final float TOLERANCE = 1.0E-5F;

  @ParameterizedTest(name = "rest {0},{1} looking {2},{3}")
  @CsvSource({
    "0, 0, 0, 0",
    "0, 0, 30, 45",
    "0, 0, -60, -70",
    "10, -15, 20, 75",
    "-25, 40, -90, 10"
  })
  @DisplayName("the look sits on the rest pose every frame and never stacks onto last frame")
  void looksFromTheRestPoseEveryFrame(
      final float restPitch, final float restYaw, final float pitch, final float yaw) {
    final GeoBone head = new GeoBone(null, "head", false, null, null, null);
    head.setRotX(restPitch * MathHelper.RADIANS_PER_DEGREE);
    head.setRotY(restYaw * MathHelper.RADIANS_PER_DEGREE);
    final AnimationProcessor<Still> processor = new AnimationProcessor<>(null);
    processor.registerGeoBone(head);
    final Still still = new Still();
    final AnimatableManager<Still> manager = new AnimatableManager<>(still);
    final AnimationState<Still> state = new AnimationState<>(still, 0, 0, 0, false);

    for (int frame = 0; frame < FRAMES; frame++) {
      processor.tickAnimation(still, null, manager, frame, state, false);
      HeadLook.apply(head, pitch, yaw);
      assertEquals((restPitch + pitch) * MathHelper.RADIANS_PER_DEGREE, head.getRotX(), TOLERANCE);
      assertEquals((restYaw + yaw) * MathHelper.RADIANS_PER_DEGREE, head.getRotY(), TOLERANCE);
    }
  }

  @ParameterizedTest(name = "looking {0},{1}")
  @CsvSource({"0, 0", "30, 45", "-60, -70"})
  @DisplayName("the look leaves the head free for the next frame's clip or rest pose")
  void leavesTheHeadUnmarked(final float pitch, final float yaw) {
    final GeoBone head = new GeoBone(null, "head", false, null, null, null);
    HeadLook.apply(head, pitch, yaw);
    assertFalse(head.hasRotationChanged());
  }

  private static final class Still implements GeoAnimatable {

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
      return null;
    }

    @Override
    public double getTick(final Object object) {
      return 0;
    }
  }
}
