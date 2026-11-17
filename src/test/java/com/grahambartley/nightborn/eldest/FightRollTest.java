package com.grahambartley.nightborn.eldest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FightRollTest {

  @ParameterizedTest(name = "{0} fighters")
  @ValueSource(ints = {0, 1, 5, 40})
  @DisplayName("the roll survives a save and load")
  void roundTrips(final int count) {
    final FightRoll roll = new FightRoll();
    final Set<UUID> joined = new HashSet<>();
    for (int i = 0; i < count; i++) {
      final UUID fighter = new UUID(i, i * 31L);
      joined.add(fighter);
      assertTrue(roll.join(fighter));
    }
    final NbtCompound nbt = new NbtCompound();
    roll.write(nbt);
    final FightRoll loaded = new FightRoll();
    loaded.join(new UUID(99, 99));
    loaded.read(nbt);
    assertEquals(joined, loaded.fighters());
  }

  @Test
  @DisplayName("a fighter joins once and is remembered")
  void joinsOnce() {
    final FightRoll roll = new FightRoll();
    final UUID fighter = new UUID(1, 2);
    assertFalse(roll.has(fighter));
    assertTrue(roll.join(fighter));
    assertFalse(roll.join(fighter));
    assertTrue(roll.has(fighter));
    assertEquals(Set.of(fighter), roll.fighters());
  }

  @Test
  @DisplayName("the roll stops growing at its limit")
  void capped() {
    final FightRoll roll = new FightRoll();
    for (int i = 0; i < FightRoll.MOST; i++) {
      assertTrue(roll.join(new UUID(7, i)));
    }
    assertFalse(roll.join(new UUID(8, 0)));
    assertEquals(FightRoll.MOST, roll.fighters().size());
  }
}
