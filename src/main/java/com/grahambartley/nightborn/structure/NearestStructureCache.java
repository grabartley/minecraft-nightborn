package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.structure.NearestStructures.Found;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;

public final class NearestStructureCache {

  public static final int MOST_KEPT = 64;

  public record Ask(Identifier dimension, Identifier tag, long chunk, int range, long day) {}

  private final Map<Ask, List<Found>> kept =
      new LinkedHashMap<>(MOST_KEPT, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(final Map.Entry<Ask, List<Found>> eldest) {
          return size() > MOST_KEPT;
        }
      };

  public synchronized Optional<List<Found>> recall(final Ask ask) {
    return Optional.ofNullable(kept.get(ask));
  }

  public synchronized void remember(final Ask ask, final List<Found> found) {
    kept.put(ask, List.copyOf(found));
  }

  public synchronized void forget() {
    kept.clear();
  }
}
