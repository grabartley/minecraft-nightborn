package com.grahambartley.nightborn.moment;

import java.util.List;

public final class MomentSchedule {

  public record Beat(MomentCue cue, float at) {

    public int tick(final int cameraTicks) {
      return Math.round(at * Math.max(0, cameraTicks));
    }
  }

  private static final List<Beat> TURNING =
      List.of(
          new Beat(MomentCue.HEARTBEAT, 0.0F),
          new Beat(MomentCue.HEARTBEAT, 0.2F),
          new Beat(MomentCue.HEARTBEAT, 0.45F),
          new Beat(MomentCue.BELL, 0.75F),
          new Beat(MomentCue.BELL, 1.0F));

  private static final List<Beat> OATH =
      List.of(new Beat(MomentCue.HORN, 0.5F), new Beat(MomentCue.HORN, 1.0F));

  private MomentSchedule() {}

  public static List<Beat> of(final Moment moment) {
    return switch (moment) {
      case TURNING -> TURNING;
      case OATH -> OATH;
    };
  }

  public static List<MomentCue> sounding(
      final Moment moment, final int tick, final int cameraTicks) {
    return of(moment).stream()
        .filter(beat -> beat.tick(cameraTicks) == tick)
        .map(Beat::cue)
        .toList();
  }
}
