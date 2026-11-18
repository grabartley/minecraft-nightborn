package com.grahambartley.nightborn.inquisitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InquisitorRosterTest {

  @ParameterizedTest(name = "{0} met, {1} fell: {2} standing")
  @CsvSource({"0, 0, 0", "1, 0, 1", "1, 1, 0", "3, 1, 2", "3, 3, 0"})
  @DisplayName(
      "every foe he met counts as standing until they fall, and the roster survives a save")
  void counts(final int met, final int fell, final int standing) {
    final InquisitorRoster roster = new InquisitorRoster();
    for (int i = 0; i < met; i++) {
      roster.meet(new UUID(0L, i));
    }
    for (int i = 0; i < fell; i++) {
      roster.fell(new UUID(0L, i));
    }
    assertEquals(standing, roster.standing());
    final NbtCompound nbt = new NbtCompound();
    roster.write(nbt);
    final InquisitorRoster loaded = new InquisitorRoster();
    loaded.read(nbt);
    assertEquals(standing, loaded.standing());
  }

  @ParameterizedTest(name = "met first {0}: falls {1}")
  @CsvSource({"true, true", "false, false"})
  @DisplayName("only a foe he met can fall in his fight, and only once")
  void fallsOnce(final boolean met, final boolean falls) {
    final InquisitorRoster roster = new InquisitorRoster();
    final UUID foe = new UUID(0L, 42L);
    if (met) {
      roster.meet(foe);
    }
    assertEquals(falls, roster.fell(foe));
    assertEquals(false, roster.fell(foe));
  }
}
