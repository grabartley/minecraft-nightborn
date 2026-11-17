package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PhaseTrackTest {

  private static List<Integer> shares(final String text) {
    return text.isBlank()
        ? List.of()
        : Arrays.stream(text.split(" ")).map(Integer::valueOf).toList();
  }

  @ParameterizedTest(name = "shares [{0}] phase {1}: from {3} down to {2}")
  @CsvSource({
    "'30 35 35', 0, 0.70, 1.00",
    "'30 35 35', 1, 0.35, 0.70",
    "'30 35 35', 2, 0.00, 0.35",
    "'1 1', 0, 0.50, 1.00",
    "'1 1', 1, 0.00, 0.50",
    "'100', 0, 0.00, 1.00",
    "'', 0, 0.00, 1.00"
  })
  @DisplayName("each phase owns its share of the health bar, from the top down")
  void bounds(final String text, final int index, final double floor, final double ceiling) {
    assertEquals(floor, PhaseTrack.floor(shares(text), index), 1.0E-9);
    assertEquals(ceiling, PhaseTrack.ceiling(shares(text), index), 1.0E-9);
  }

  @ParameterizedTest(name = "{0} damage at {1} health over a floor of {2}, uncapped {3}: {4}")
  @CsvSource({
    "10, 100, 70, false, 10",
    "40, 100, 70, false, 30",
    "40, 70, 70, false, 0",
    "40, 60, 70, false, 0",
    "40, 100, 70, true, 40",
    "500, 20, 0, false, 20"
  })
  @DisplayName("a blow cannot carry past the end of a phase unless it is uncapped")
  void caps(
      final float amount,
      final float health,
      final float floor,
      final boolean uncapped,
      final float dealt) {
    assertEquals(dealt, PhaseTrack.cap(amount, health, floor, uncapped), 1.0E-6F);
  }

  @ParameterizedTest(name = "health {0} over a floor of {1}, last phase {2}: spent {3}")
  @CsvSource({
    "70.0, 70, false, true",
    "70.5, 70, false, true",
    "70.6, 70, false, false",
    "100, 70, false, false",
    "0.2, 0, true, false",
    "0.2, 0, false, true"
  })
  @DisplayName("a phase is spent once health reaches its floor, and the last phase never is")
  void spent(final float health, final float floor, final boolean last, final boolean spent) {
    assertEquals(spent, PhaseTrack.spent(health, floor, last));
  }

  @ParameterizedTest(name = "health {0} between {1} and {2} fills {3}")
  @CsvSource({
    "100, 70, 100, 1.0",
    "85, 70, 100, 0.5",
    "70, 70, 100, 0.0",
    "60, 70, 100, 0.0",
    "120, 70, 100, 1.0",
    "50, 50, 50, 0.0"
  })
  @DisplayName("the bar shows how much of the current phase is left")
  void barPercent(final float health, final float floor, final float ceiling, final float fill) {
    assertEquals(fill, PhaseTrack.barPercent(health, floor, ceiling), 1.0E-6F);
  }

  @ParameterizedTest(name = "{0} advances of {1} phases ends on phase {2}, last {3}")
  @CsvSource({
    "0, 3, 0, false",
    "1, 3, 1, false",
    "2, 3, 2, true",
    "5, 3, 2, true",
    "3, 1, 0, true"
  })
  @DisplayName("the track steps through the phases and stops on the last")
  void advances(final int advances, final int phases, final int index, final boolean last) {
    final PhaseTrack track = new PhaseTrack();
    for (int i = 0; i < advances; i++) {
      track.advance(phases);
    }
    assertEquals(index, track.index());
    assertEquals(last, track.last(phases));
  }

  @Test
  @DisplayName("the phase survives a save, and a reset goes back to the first")
  void savesAndResets() {
    final PhaseTrack track = new PhaseTrack();
    track.advance(3);
    track.advance(3);
    final NbtCompound nbt = new NbtCompound();
    track.write(nbt);
    final PhaseTrack loaded = new PhaseTrack();
    loaded.read(nbt);
    assertEquals(2, loaded.index());
    loaded.reset();
    assertEquals(0, loaded.index());
    final NbtCompound bad = new NbtCompound();
    bad.putInt(PhaseTrack.KEY, -4);
    loaded.read(bad);
    assertEquals(0, loaded.index());
  }
}
