package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HouseholdLifeTest {

  private static final DynamicRegistryManager NONE = DynamicRegistryManager.EMPTY;

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
    assertEquals(grieving, reloaded(life).grieving(asked));
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

  @ParameterizedTest(name = "talking until {0}, stopped {1}, asked at {2}: {3}")
  @CsvSource({"200, false, 150, true", "200, false, 200, false", "200, true, 150, false"})
  @DisplayName("a vampire that starts a conversation is busy until it ends")
  void talking(final long until, final boolean stopped, final long now, final boolean talking) {
    final HouseholdLife life = new HouseholdLife();
    life.startTalking(until);
    if (stopped) {
      life.stopTalking();
    }
    assertEquals(talking, life.talking(now));
  }

  @ParameterizedTest(name = "roll {0}")
  @CsvSource({"0.0", "0.5", "0.99"})
  @DisplayName("with no temperament data a vampire is even tempered, but saves no temperament")
  void fallbackIsNotSaved(final double roll) {
    final HouseholdLife life = new HouseholdLife();
    assertEquals(Temperaments.FALLBACK, life.temperamentId(NONE, () -> roll));
    assertEquals(Temperament.EVEN, life.temperament(NONE, () -> roll));
    final NbtCompound nbt = new NbtCompound();
    life.write(nbt, NONE);
    assertEquals(false, nbt.contains(HouseholdLife.TEMPERAMENT_KEY));
  }

  @ParameterizedTest(name = "skipped until {0}, asked at {1}: {2}")
  @CsvSource({"500, 100, true", "500, 499, true", "500, 500, false", "500, 900, false"})
  @DisplayName("a chore a vampire gave up on is left alone for a while")
  void skipsChores(final long until, final long now, final boolean skips) {
    final HouseholdLife life = new HouseholdLife();
    final BlockPos spot = new BlockPos(1, 2, 3);
    life.skip(spot, until);
    assertEquals(skips, life.skips(spot, now));
    assertEquals(false, life.skips(spot.up(), now));
  }

  private static HouseholdLife reloaded(final HouseholdLife life) {
    final NbtCompound nbt = new NbtCompound();
    life.write(nbt, NONE);
    final HouseholdLife loaded = new HouseholdLife();
    loaded.read(nbt, NONE);
    return loaded;
  }
}
