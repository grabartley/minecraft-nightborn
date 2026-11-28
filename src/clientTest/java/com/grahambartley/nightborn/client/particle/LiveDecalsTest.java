package com.grahambartley.nightborn.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LiveDecalsTest {

  @ParameterizedTest(name = "limit {0}, {1} placed apart: {2} retiring, {3} kept")
  @CsvSource({"3, 2, '', 2", "3, 3, '', 3", "3, 4, 0, 4", "3, 6, 0 1 2, 6", "1, 3, 0 1, 3"})
  @DisplayName("past the limit the oldest decals start to fade, newest first kept")
  void retiresTheOldest(final int limit, final int placed, final String retiring, final int kept) {
    final LiveDecals<Fake> live = new LiveDecals<>(limit);
    final List<Fake> decals = new ArrayList<>();
    for (int index = 0; index < placed; index++) {
      final Fake decal = new Fake(index * 10);
      decals.add(decal);
      live.place(decal, 0L);
    }
    assertEquals(retiring, retired(decals));
    assertEquals(kept, live.size());
  }

  @ParameterizedTest(name = "a decal at {0} lands by one at 0 and one at 3: joins {1}")
  @CsvSource({"0, 0", "1, 3", "3, 3", "4, 3", "9, ''"})
  @DisplayName("a decal landing on a newer one joins that one before an older one")
  void joinsTheNewestItFits(final int at, final String joined) {
    final LiveDecals<Fake> live = new LiveDecals<>(10);
    final List<Fake> decals = List.of(new Fake(0), new Fake(3));
    decals.forEach(decal -> live.place(decal, 0L));
    final boolean added = live.place(new Fake(at), 0L);
    assertEquals(joined.isEmpty(), added);
    assertEquals(
        joined,
        decals.stream()
            .filter(decal -> decal.absorbed > 0)
            .map(decal -> String.valueOf(decal.at))
            .collect(Collectors.joining(" ")));
  }

  @ParameterizedTest(name = "gone {0}, retiring {1}: joined {2}, size after {3}")
  @CsvSource({"false, false, true, 1", "true, false, false, 1", "false, true, false, 2"})
  @DisplayName("gone decals are dropped and fading ones take no new blood")
  void skipsGoneAndFadingDecals(
      final boolean gone, final boolean retiring, final boolean joined, final int size) {
    final LiveDecals<Fake> live = new LiveDecals<>(10);
    final Fake old = new Fake(0);
    live.place(old, 0L);
    old.gone = gone;
    old.retiring = retiring;
    live.place(new Fake(1), 0L);
    assertEquals(joined, old.absorbed > 0);
    assertEquals(size, live.size());
  }

  private static String retired(final List<Fake> decals) {
    final List<String> indexes = new ArrayList<>();
    for (int index = 0; index < decals.size(); index++) {
      if (decals.get(index).retiring) {
        indexes.add(String.valueOf(index));
      }
    }
    return String.join(" ", indexes);
  }

  private static final class Fake implements PlacedDecal<Fake> {
    private final int at;
    private boolean gone;
    private boolean retiring;
    private int absorbed;

    private Fake(final int at) {
      this.at = at;
    }

    @Override
    public boolean gone(final long now) {
      return gone;
    }

    @Override
    public boolean retiring() {
      return retiring;
    }

    @Override
    public void retire() {
      retiring = true;
    }

    @Override
    public boolean absorbs(final Fake incoming) {
      return Math.abs(incoming.at - at) <= 2;
    }

    @Override
    public void absorb(final Fake incoming) {
      absorbed++;
    }
  }
}
