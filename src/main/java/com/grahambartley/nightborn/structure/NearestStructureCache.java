package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.structure.NearestStructures.Found;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.util.Identifier;

public final class NearestStructureCache {

  public static final int MOST_KEPT = 64;

  public record Ask(Identifier dimension, Identifier tag, long chunk, int range, long day) {}

  private final Map<Ask, CompletableFuture<List<Found>>> kept =
      new LinkedHashMap<>(MOST_KEPT, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(
            final Map.Entry<Ask, CompletableFuture<List<Found>>> eldest) {
          return size() > MOST_KEPT;
        }
      };

  public synchronized CompletableFuture<List<Found>> find(
      final Ask ask, final Supplier<CompletableFuture<List<Found>>> search) {
    final CompletableFuture<List<Found>> known = kept.get(ask);
    if (known != null && !known.isCompletedExceptionally()) {
      return known;
    }
    final CompletableFuture<List<Found>> started = search.get().thenApply(List::copyOf);
    kept.put(ask, started);
    return started;
  }

  public synchronized void forget() {
    kept.clear();
  }
}
