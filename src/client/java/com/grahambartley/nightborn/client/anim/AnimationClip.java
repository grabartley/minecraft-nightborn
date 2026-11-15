package com.grahambartley.nightborn.client.anim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record AnimationClip(String name, double length, boolean loops, Map<String, Track> tracks) {

  public record Track(ClipChannel rotation, ClipChannel position) {}

  public AnimationClip {
    tracks = Map.copyOf(tracks);
  }

  public boolean finishedAt(final double seconds) {
    return !loops && seconds >= length;
  }

  public RigPose sample(final double seconds) {
    final double time =
        loops && length > 0.0
            ? ((seconds % length) + length) % length
            : Math.max(0.0, Math.min(length, seconds));
    final Map<String, BonePose> bones = new HashMap<>();
    tracks.forEach(
        (bone, track) ->
            bones.put(
                bone, new BonePose(track.rotation().sample(time), track.position().sample(time))));
    return new RigPose(bones);
  }

  public List<Double> peaksOf(final String bone, final double atLeastDegrees) {
    final Track track = tracks.get(bone);
    return track == null ? List.of() : track.rotation().peakTimesOfX(atLeastDegrees);
  }
}
