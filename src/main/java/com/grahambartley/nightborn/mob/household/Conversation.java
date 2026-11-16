package com.grahambartley.nightborn.mob.household;

import java.util.ArrayList;
import java.util.List;

public final class Conversation {

  static final int FEWEST_LINES = 3;
  static final int MOST_LINES = 6;

  public enum Tone {
    TALK("talk"),
    ARGUE("argue");

    private final String clip;

    Tone(final String clip) {
      this.clip = clip;
    }

    public String clip() {
      return clip;
    }
  }

  public record Line(boolean opener, Tone tone) {}

  private Conversation() {}

  public static List<Line> lines(
      final int lengthRoll, final int quarrelRoll, final int temper, final int otherTemper) {
    final int count = FEWEST_LINES + Math.floorMod(lengthRoll, MOST_LINES - FEWEST_LINES + 1);
    final boolean quarrel = quarrelRoll < Math.max(temper, otherTemper);
    final int turns = quarrel ? Math.max(1, count / 2) : count;
    final List<Line> lines = new ArrayList<>(count);
    for (int index = 0; index < count; index++) {
      lines.add(new Line(index % 2 == 0, index < turns ? Tone.TALK : Tone.ARGUE));
    }
    return List.copyOf(lines);
  }
}
