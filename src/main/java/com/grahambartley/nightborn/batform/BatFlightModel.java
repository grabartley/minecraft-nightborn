package com.grahambartley.nightborn.batform;

import com.grahambartley.nightborn.levitation.LevitationLookAhead;
import net.minecraft.util.math.Vec3d;

public final class BatFlightModel {

  static final double CRUISE_SPEED = 0.4;
  static final double SPRINT_SPEED = 0.7;
  static final double PITCH_WEIGHT = 0.3;
  static final double SPEED_UP = 0.03;
  static final double EASE_OFF = 0.02;
  static final double STEER = 0.2;
  static final double TURN_SLOWING = 0.5;
  static final double ABOUT_FACE = -0.999;
  static final double COAST_DRAG = 0.92;
  static final double BRAKE_DRAG = 0.8;
  static final double STILL = 1.0E-3;
  static final double CRAWL_SPEED = 0.04;
  static final double GRIP = 0.08;
  static final double TAKE_OFF = 0.25;

  private BatFlightModel() {}

  public record Controls(
      Vec3d look, float yaw, double forward, double strafe, boolean sprinting, boolean jumping) {}

  public static Vec3d next(final Vec3d velocity, final boolean onGround, final Controls controls) {
    return onGround ? crawl(velocity, controls) : fly(velocity, controls);
  }

  static Vec3d fly(final Vec3d velocity, final Controls controls) {
    if (controls.forward() <= 0.0) {
      return coast(velocity, controls.forward() < 0.0 ? BRAKE_DRAG : COAST_DRAG);
    }
    final Vec3d look = controls.look();
    final double speed = velocity.length();
    final Vec3d heading = speed > STILL ? velocity.multiply(1.0 / speed) : look;
    final double slowed = speed * turnKeep(heading.dotProduct(look));
    return steer(heading, look).multiply(ease(slowed, targetSpeed(controls.sprinting(), look.y)));
  }

  static Vec3d coast(final Vec3d velocity, final double drag) {
    final Vec3d slowed = velocity.multiply(drag);
    return slowed.length() < STILL ? Vec3d.ZERO : slowed;
  }

  static double targetSpeed(final boolean sprinting, final double lookY) {
    return (sprinting ? SPRINT_SPEED : CRUISE_SPEED) * (1.0 - PITCH_WEIGHT * lookY);
  }

  static double turnKeep(final double alignment) {
    return 1.0 - TURN_SLOWING * (1.0 - alignment) / 2.0;
  }

  static double ease(final double speed, final double target) {
    return speed < target ? Math.min(target, speed + SPEED_UP) : Math.max(target, speed - EASE_OFF);
  }

  static Vec3d steer(final Vec3d heading, final Vec3d look) {
    return heading.dotProduct(look) < ABOUT_FACE ? look : heading.lerp(look, STEER).normalize();
  }

  static Vec3d crawl(final Vec3d velocity, final Controls controls) {
    if (controls.jumping()) {
      return new Vec3d(velocity.x, TAKE_OFF, velocity.z);
    }
    return LevitationLookAhead.heading(controls.strafe(), controls.forward(), controls.yaw())
        .map(heading -> new Vec3d(heading.x() * CRAWL_SPEED, -GRIP, heading.z() * CRAWL_SPEED))
        .orElse(new Vec3d(0.0, -GRIP, 0.0));
  }
}
