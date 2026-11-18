package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.nightborn.structure.NearestStructures.Found;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NearestStructureCacheTest {

  static NearestStructureCache.Ask ask(final long chunk, final int range, final long day) {
    return new NearestStructureCache.Ask(
        Identifier.ofVanilla("overworld"),
        Identifier.of("nightborn", "vampire_houses"),
        chunk,
        range,
        day);
  }

  static final List<Found> CRYPT =
      List.of(new Found(Identifier.of("nightborn", "crypt"), new BlockPos(400, 0, 0)));

  static final class Searches implements Supplier<CompletableFuture<List<Found>>> {
    final AtomicInteger started = new AtomicInteger();
    CompletableFuture<List<Found>> next = CompletableFuture.completedFuture(CRYPT);

    @Override
    public CompletableFuture<List<Found>> get() {
      started.incrementAndGet();
      return next;
    }
  }

  @ParameterizedTest(name = "chunk {0}, range {1}, day {2} -> searches {3}")
  @CsvSource({"5, 256, 3, 1", "6, 256, 3, 2", "5, 512, 3, 2", "5, 256, 4, 2"})
  @DisplayName(
      "a search is reused only for the same chunk, range and day, so asking again is cheap")
  void reuse(final long chunk, final int range, final long day, final int searches) {
    final NearestStructureCache cache = new NearestStructureCache();
    final Searches search = new Searches();
    cache.find(ask(5, 256, 3), search);
    assertEquals(CRYPT, cache.find(ask(chunk, range, day), search).join());
    assertEquals(searches, search.started.get());
  }

  @Test
  @DisplayName("asking again while a search still runs waits on that search, not a new one")
  void running() {
    final NearestStructureCache cache = new NearestStructureCache();
    final Searches search = new Searches();
    final CompletableFuture<List<Found>> pending = new CompletableFuture<>();
    search.next = pending;
    final CompletableFuture<List<Found>> first = cache.find(ask(5, 256, 3), search);
    assertSame(first, cache.find(ask(5, 256, 3), search));
    pending.complete(CRYPT);
    assertEquals(CRYPT, first.join());
    assertEquals(1, search.started.get());
  }

  @Test
  @DisplayName("a failed search is tried again on the next ask")
  void failed() {
    final NearestStructureCache cache = new NearestStructureCache();
    final Searches search = new Searches();
    search.next = CompletableFuture.failedFuture(new IllegalStateException("chunk unloaded"));
    cache.find(ask(5, 256, 3), search);
    search.next = CompletableFuture.completedFuture(CRYPT);
    assertEquals(CRYPT, cache.find(ask(5, 256, 3), search).join());
    assertEquals(2, search.started.get());
  }

  @Test
  @DisplayName("the cache keeps only the most recent searches and empties when told")
  void bounded() {
    final NearestStructureCache cache = new NearestStructureCache();
    final Searches search = new Searches();
    for (long chunk = 0; chunk <= NearestStructureCache.MOST_KEPT; chunk++) {
      cache.find(ask(chunk, 256, 0), search);
    }
    final int filled = search.started.get();
    cache.find(ask(NearestStructureCache.MOST_KEPT, 256, 0), search);
    assertEquals(filled, search.started.get());
    cache.find(ask(0, 256, 0), search);
    assertEquals(filled + 1, search.started.get());
    cache.forget();
    cache.find(ask(NearestStructureCache.MOST_KEPT, 256, 0), search);
    assertEquals(filled + 2, search.started.get());
  }
}
