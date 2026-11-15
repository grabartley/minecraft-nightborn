package com.grahambartley.nightborn.client.anim;

import com.grahambartley.nightborn.mob.LandingEdge;
import com.grahambartley.nightborn.mob.VampirePose;
import java.util.List;

public final class PlayerMotion {

  public static final double TICKS_PER_SECOND = 20.0;
  static final double BLEND_SHARE = 0.2;
  static final double RISE_SHARE = 0.25;
  static final double NOT_PLAYING = Double.NaN;

  private final LandingEdge landing = new LandingEdge();
  private boolean levitating;
  private double previousLift;
  private double lift;
  private LevitationBlend previousBlend = LevitationBlend.STILL;
  private LevitationBlend blend = LevitationBlend.STILL;
  private double landingFrom = NOT_PLAYING;
  private double vomitingFrom = NOT_PLAYING;
  private double lastTick;

  public void tick(
      final double age,
      final boolean nowLevitating,
      final double horizontalSpeed,
      final boolean sprinting) {
    if (landing.landed(nowLevitating)) {
      landingFrom = age;
      lift = 0.0;
    }
    if (nowLevitating) {
      landingFrom = NOT_PLAYING;
    }
    levitating = nowLevitating;
    previousLift = lift;
    lift = nowLevitating ? lift + (1.0 - lift) * RISE_SHARE : 0.0;
    previousBlend = blend;
    blend = blend.approach(LevitationBlend.target(horizontalSpeed, sprinting), BLEND_SHARE);
    lastTick = age;
  }

  public void startVomiting(final double age) {
    vomitingFrom = age;
  }

  public double vomitingSeconds(final double age) {
    return Double.isNaN(vomitingFrom) ? NOT_PLAYING : (age - vomitingFrom) / TICKS_PER_SECOND;
  }

  public boolean idle(final ClipSet clips, final double age) {
    return !levitating
        && finished(landingFrom, clips.length(VampirePose.LAND), age)
        && finished(vomitingFrom, clips.length(VampirePose.VOMIT), age);
  }

  public RigPose pose(final ClipSet clips, final double age) {
    final double partial = Math.max(0.0, Math.min(1.0, age - lastTick));
    final double seconds = age / TICKS_PER_SECOND;
    RigPose pose = RigPose.REST;
    final double weight = previousLift + (lift - previousLift) * partial;
    if (weight > 0.0) {
      final LevitationBlend mix = previousBlend.approach(blend, partial);
      pose =
          clips
              .sample(VampirePose.LEVITATE, seconds)
              .towards(clips.sample(VampirePose.LEVITATE_MOVE, seconds), mix.move())
              .towards(clips.sample(VampirePose.LEVITATE_SPRINT, seconds), mix.sprint())
              .times(weight);
    }
    pose = pose.plus(playing(clips, VampirePose.LAND, landingFrom, age));
    return pose.plus(playing(clips, VampirePose.VOMIT, vomitingFrom, age));
  }

  public List<Double> heaves(final ClipSet clips, final double degrees) {
    final List<Double> peaks =
        clips.clip(VampirePose.VOMIT).map(clip -> clip.peaksOf("body", degrees)).orElse(List.of());
    return peaks.isEmpty() ? List.of(0.0) : peaks;
  }

  public List<Double> heavesCrossed(
      final ClipSet clips, final double degrees, final double before, final double now) {
    if (Double.isNaN(now)) {
      return List.of();
    }
    return heaves(clips, degrees).stream()
        .filter(heave -> (Double.isNaN(before) || before < heave) && now >= heave)
        .toList();
  }

  private static RigPose playing(
      final ClipSet clips, final VampirePose pose, final double from, final double age) {
    if (finished(from, clips.length(pose), age)) {
      return RigPose.REST;
    }
    return clips.sample(pose, (age - from) / TICKS_PER_SECOND);
  }

  private static boolean finished(final double from, final double length, final double age) {
    return Double.isNaN(from) || (age - from) / TICKS_PER_SECOND >= length;
  }
}
