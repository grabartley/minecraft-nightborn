package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.mob.VampirePose;
import java.util.List;
import java.util.Map;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlayerMotionTest {

  private static AnimationClip hold(
      final VampirePose pose, final String bone, final double x, final double length) {
    return new AnimationClip(
        pose.animation(),
        length,
        length > 1.0,
        Map.of(
            bone,
            new AnimationClip.Track(
                new ClipChannel(List.of(new ClipChannel.Key(0.0, new Vec3d(x, 0, 0), false))),
                ClipChannel.EMPTY)));
  }

  private static final ClipSet CLIPS =
      new ClipSet(
          Map.of(
              "levitate", hold(VampirePose.LEVITATE, "root", 2, 3.0),
              "levitate_move", hold(VampirePose.LEVITATE_MOVE, "root", 16, 2.0),
              "levitate_sprint", hold(VampirePose.LEVITATE_SPRINT, "root", 34, 1.2),
              "land", hold(VampirePose.LAND, "body", -9, 0.5),
              "vomit",
                  new AnimationClip(
                      "vomit",
                      1.0,
                      false,
                      Map.of(
                          "body",
                          new AnimationClip.Track(
                              new ClipChannel(
                                  List.of(
                                      new ClipChannel.Key(0.0, Vec3d.ZERO, false),
                                      new ClipChannel.Key(0.4, new Vec3d(44, 0, 0), false),
                                      new ClipChannel.Key(0.6, new Vec3d(30, 0, 0), false),
                                      new ClipChannel.Key(0.8, new Vec3d(50, 0, 0), false),
                                      new ClipChannel.Key(1.0, Vec3d.ZERO, false))),
                              ClipChannel.EMPTY)))),
          RigGeometry.NONE);

  @Test
  @DisplayName("a vampire on the ground with nothing playing is at rest")
  void restOnTheGround() {
    final PlayerMotion motion = new PlayerMotion();
    motion.tick(0, false, 0.0, false);
    assertTrue(motion.pose(CLIPS, 0).isRest());
    assertTrue(motion.idle(CLIPS, 0));
  }

  @Test
  @DisplayName("levitating eases into the float rather than snapping")
  void easesIn() {
    final PlayerMotion motion = new PlayerMotion();
    motion.tick(0, true, 0.0, false);
    motion.tick(1, true, 0.0, false);
    final double first = motion.pose(CLIPS, 1).bone("root").rotation().x;
    for (int tick = 1; tick <= 40; tick++) {
      motion.tick(tick, true, 0.0, false);
    }
    final double settled = motion.pose(CLIPS, 40).bone("root").rotation().x;
    assertTrue(first > 0.0 && first < settled);
    assertEquals(2.0, settled, 0.01);
  }

  @ParameterizedTest(name = "speed {0}, sprinting {1}: lean {2}")
  @CsvSource({"0, false, 2", "0.3, false, 16", "0.3, true, 34"})
  @DisplayName("moving leans into the move clip and sprinting into the sprint clip")
  void leansWithSpeed(final double speed, final boolean sprinting, final double lean) {
    final PlayerMotion motion = new PlayerMotion();
    for (int tick = 0; tick <= 80; tick++) {
      motion.tick(tick, true, speed, sprinting);
    }
    assertEquals(lean, motion.pose(CLIPS, 80).bone("root").rotation().x, 0.05);
  }

  @Test
  @DisplayName("stopping plays the landing once, then rests")
  void landsOnce() {
    final PlayerMotion motion = new PlayerMotion();
    for (int tick = 0; tick <= 20; tick++) {
      motion.tick(tick, true, 0.0, false);
    }
    motion.tick(21, false, 0.0, false);
    assertEquals(-9, motion.pose(CLIPS, 22).bone("body").rotation().x, 1.0E-9);
    assertEquals(0, motion.pose(CLIPS, 22).bone("root").rotation().x, 1.0E-9);
    assertFalse(motion.idle(CLIPS, 22));
    assertTrue(motion.idle(CLIPS, 31));
    assertTrue(motion.pose(CLIPS, 31).isRest());
  }

  @Test
  @DisplayName("vomiting plays its clip from the start, finds its heaves and ends")
  void vomits() {
    final PlayerMotion motion = new PlayerMotion();
    motion.startVomiting(100);
    assertEquals(44, motion.pose(CLIPS, 108).bone("body").rotation().x, 1.0E-9);
    assertEquals(0.4, motion.vomitingSeconds(108), 1.0E-9);
    assertEquals(List.of(0.4, 0.8), motion.heaves(CLIPS, 30));
    assertTrue(motion.idle(CLIPS, 120));
  }

  @Test
  @DisplayName("nothing was ever vomited, so no time has passed")
  void notVomiting() {
    assertTrue(Double.isNaN(new PlayerMotion().vomitingSeconds(5)));
  }
}
