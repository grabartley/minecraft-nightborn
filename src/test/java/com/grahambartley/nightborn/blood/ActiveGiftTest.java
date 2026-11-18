package com.grahambartley.nightborn.blood;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ActiveGiftTest {

  @ParameterizedTest(name = "{0} at {1}, then {2} at {3}: refresh {4}")
  @CsvSource({
    "nightborn:horse, 1.0, nightborn:horse, 1.0, true",
    "nightborn:horse, 1.0, nightborn:horse, 0.5, false",
    "nightborn:horse, 0.5, nightborn:wolf, 0.5, false"
  })
  @DisplayName("the same gift at the same strength refreshes, anything else replaces it")
  void refreshes(
      final String gift,
      final float strength,
      final String other,
      final float otherStrength,
      final boolean refreshes) {
    assertEquals(
        refreshes,
        new ActiveGift(Identifier.of(gift), strength)
            .refreshes(Identifier.of(other), otherStrength));
  }

  @ParameterizedTest(name = "{0} at {1}")
  @CsvSource({"nightborn:witch, 1.0", "nightborn:wolf, 0.5"})
  @DisplayName("an active gift survives a save and load")
  void roundTrips(final String gift, final float strength) {
    final ActiveGift active = new ActiveGift(Identifier.of(gift), strength);
    assertEquals(
        active,
        ActiveGift.CODEC
            .parse(
                JsonOps.INSTANCE,
                ActiveGift.CODEC.encodeStart(JsonOps.INSTANCE, active).getOrThrow())
            .getOrThrow());
  }
}
