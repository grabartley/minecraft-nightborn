package com.grahambartley.nightborn.raid;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NoiseLevels {

  private record Heard(double level, long at) {}

  private final Map<UUID, Heard> heard = new HashMap<>();

  public double hear(
      final UUID sleeper, final double loudness, final long now, final int fadePerSecond) {
    final Heard before = heard.get(sleeper);
    final double level =
        before == null
            ? Noise.heard(0.0, 0L, fadePerSecond, loudness)
            : Noise.heard(before.level(), now - before.at(), fadePerSecond, loudness);
    heard.put(sleeper, new Heard(level, now));
    return level;
  }

  public double level(final UUID sleeper, final long now, final int fadePerSecond) {
    final Heard before = heard.get(sleeper);
    return before == null ? 0.0 : Noise.faded(before.level(), now - before.at(), fadePerSecond);
  }

  public void forget(final UUID sleeper) {
    heard.remove(sleeper);
  }

  public void settle(final long now, final int fadePerSecond) {
    heard
        .entrySet()
        .removeIf(
            entry ->
                Noise.faded(entry.getValue().level(), now - entry.getValue().at(), fadePerSecond)
                    <= 0.0);
  }

  public int size() {
    return heard.size();
  }

  public void clear() {
    heard.clear();
  }
}
