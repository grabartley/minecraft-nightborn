package com.grahambartley.nightborn.inquisitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InquisitorQuarryTest {

  static final UUID RINGER = UUID.fromString("00000000-0000-0000-0000-000000000007");
  static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000008");

  @ParameterizedTest(name = "bell {0}, ringer a vampire {1}: turned on {2}")
  @CsvSource({"true, false, true", "true, true, false", "false, true, false"})
  @DisplayName("only a human who rang is turned on, and nobody else")
  void turnsOn(final boolean bell, final boolean vampire, final boolean turned) {
    final InquisitorQuarry quarry = InquisitorQuarry.of(RINGER, bell, vampire);
    assertEquals(turned, quarry.turnedOn(RINGER));
    assertEquals(false, quarry.turnedOn(OTHER));
  }

  @ParameterizedTest(name = "bell {0}, ringer a vampire {1}")
  @CsvSource({"true, false", "true, true", "false, true"})
  @DisplayName("his quarry survives a save and a load")
  void roundTrips(final boolean bell, final boolean vampire) {
    final InquisitorQuarry quarry = InquisitorQuarry.of(RINGER, bell, vampire);
    final NbtCompound nbt = new NbtCompound();
    quarry.write(nbt);
    assertEquals(Optional.of(quarry), InquisitorQuarry.read(nbt));
    assertEquals(Optional.empty(), InquisitorQuarry.read(new NbtCompound()));
  }
}
