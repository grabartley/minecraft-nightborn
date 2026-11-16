package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HouseholdLifeTest {

  @ParameterizedTest(name = "grieves {1}s from {0}, asked at {2}: {3}")
  @CsvSource({
    "100, 10, 100, true",
    "100, 10, 299, true",
    "100, 10, 300, false",
    "100, 0, 100, false"
  })
  @DisplayName("a vampire grieves for its temperament's mourning time, which survives a reload")
  void grieves(final long now, final int seconds, final long asked, final boolean grieving) {
    final HouseholdLife life = new HouseholdLife();
    life.grieve(now, seconds);
    final NbtCompound nbt = new NbtCompound();
    life.write(nbt);
    final HouseholdLife loaded = new HouseholdLife();
    loaded.read(nbt);
    assertEquals(grieving, loaded.grieving(asked));
  }

  @ParameterizedTest(name = "engaged by {0} until {1}, asked at {2}: {3}")
  @CsvSource({"7, 200, 150, true", "7, 200, 200, false", "7, 200, 250, false"})
  @DisplayName("a housemate drawn into a conversation is free again once it ends")
  void engagement(final int partner, final long until, final long now, final boolean engaged) {
    final HouseholdLife life = new HouseholdLife();
    life.engage(partner, until);
    assertEquals(
        engaged ? Optional.of(new HouseholdLife.Engagement(partner, until)) : Optional.empty(),
        life.engagement(now));
  }

  @ParameterizedTest(name = "borrowed {0}")
  @CsvSource({"true", "false"})
  @DisplayName(
      "a book or flint and steel lent for a pastime is remembered through a reload, to be put back")
  void remembersBorrowedHand(final boolean borrowed) {
    final HouseholdLife life = new HouseholdLife();
    life.borrowHand(borrowed);
    final NbtCompound nbt = new NbtCompound();
    life.write(nbt);
    final HouseholdLife loaded = new HouseholdLife();
    loaded.read(nbt);
    assertEquals(borrowed, loaded.handBorrowed());
  }

  @ParameterizedTest(name = "roll {0}")
  @CsvSource({"0.0", "0.5", "0.99"})
  @DisplayName("a vampire keeps the temperament it was given, through a reload")
  void keepsTemperament(final double roll) {
    final HouseholdLife life = new HouseholdLife();
    assertEquals(
        Temperaments.FALLBACK, life.temperamentId(DynamicRegistryManager.EMPTY, () -> roll));
    final NbtCompound nbt = new NbtCompound();
    life.write(nbt);
    final HouseholdLife loaded = new HouseholdLife();
    loaded.read(nbt);
    assertEquals(
        Temperaments.FALLBACK, loaded.temperamentId(DynamicRegistryManager.EMPTY, () -> 1.0));
    assertEquals(Temperament.EVEN, loaded.temperament(DynamicRegistryManager.EMPTY, () -> roll));
  }
}
