package com.grahambartley.nightborn.client.anim;

import java.util.List;

public final class ClipTrack {

  public record Segment(double ticks, double start, double end) {}

  private ClipTrack() {}

  public static double at(final List<Segment> segments, final double ticks) {
    double elapsed = 0.0;
    for (final Segment segment : segments) {
      if (ticks < elapsed + segment.ticks()) {
        final double share = segment.ticks() <= 0.0 ? 1.0 : (ticks - elapsed) / segment.ticks();
        return segment.start() + (segment.end() - segment.start()) * Math.max(0.0, share);
      }
      elapsed += segment.ticks();
    }
    return segments.isEmpty() ? 0.0 : segments.getLast().end();
  }
}
