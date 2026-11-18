package com.grahambartley.nightborn.skill;

import java.util.Map;
import net.minecraft.util.Identifier;

public final class LineShare {

  private LineShare() {}

  public static int percent(
      final Map<Identifier, Skill> skills, final TreeProgress progress, final Identifier line) {
    final var inLine =
        skills.entrySet().stream().filter(entry -> entry.getValue().branch().equals(line)).toList();
    return percent(
        (int) inLine.stream().filter(entry -> progress.owns(entry.getKey())).count(),
        inLine.size());
  }

  public static int percent(final int owned, final int total) {
    return total <= 0 ? 0 : Math.min(100, owned * 100 / total);
  }
}
