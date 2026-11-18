package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HouseholdRollTest {

  @ParameterizedTest(name = "{0} households")
  @ValueSource(ints = {0, 1, 4})
  @DisplayName("every household's living and head's coffin survive a save and load")
  void roundTrip(final int count) {
    final HouseholdRoll roll = new HouseholdRoll();
    for (int i = 0; i < count; i++) {
      final GlobalPos home = GlobalPos.create(World.OVERWORLD, new BlockPos(i * 100, 50, 0));
      roll.enlist(home, new UUID(0, i));
      roll.headSleepsAt(home, GlobalPos.create(World.OVERWORLD, new BlockPos(i * 100, 50, 3)));
    }
    final HouseholdRoll loaded =
        HouseholdRoll.fromNbt(roll.writeNbt(new NbtCompound(), null), null);
    for (int i = 0; i < count; i++) {
      final GlobalPos home = GlobalPos.create(World.OVERWORLD, new BlockPos(i * 100, 50, 0));
      assertEquals(roll.record(home), loaded.record(home));
    }
  }

  @ParameterizedTest(name = "forget {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("the roll is marked to save only when a household changes")
  void dirty(final boolean forget) {
    final HouseholdRoll roll = new HouseholdRoll();
    final GlobalPos home = GlobalPos.create(World.OVERWORLD, BlockPos.ORIGIN);
    roll.enlist(home, new UUID(0, 1));
    roll.setDirty(false);
    roll.enlist(home, new UUID(0, 1));
    assertEquals(false, roll.isDirty());
    if (forget) {
      roll.forget(home, new UUID(0, 1));
    }
    assertEquals(forget, roll.isDirty());
  }

  @ParameterizedTest(name = "known {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("a refill time is kept only for a household the roll already knows")
  void refillAt(final boolean known) {
    final HouseholdRoll roll = new HouseholdRoll();
    final GlobalPos home = GlobalPos.create(World.OVERWORLD, BlockPos.ORIGIN);
    if (known) {
      roll.enlist(home, new UUID(0, 1));
      roll.forget(home, new UUID(0, 1));
    }
    roll.refillAt(home, 480_000L);
    assertEquals(known, roll.known(home));
    assertEquals(known ? Optional.of(480_000L) : Optional.empty(), roll.record(home).refillAt());
  }

  @ParameterizedTest(name = "{0} arrivals")
  @ValueSource(ints = {1, 3})
  @DisplayName("a household that moves in is saved as the structure's living")
  void refilled(final int count) {
    final HouseholdRoll roll = new HouseholdRoll();
    final GlobalPos home = GlobalPos.create(World.OVERWORLD, BlockPos.ORIGIN);
    roll.enlist(home, new UUID(0, 9));
    roll.forget(home, new UUID(0, 9));
    roll.refillAt(home, 10L);
    final Set<UUID> arrivals =
        IntStream.range(0, count).mapToObj(i -> new UUID(1, i)).collect(Collectors.toSet());
    roll.refilled(home, arrivals);
    final HouseholdRoll loaded =
        HouseholdRoll.fromNbt(roll.writeNbt(new NbtCompound(), null), null);
    assertEquals(arrivals, loaded.record(home).living());
    assertEquals(Optional.empty(), loaded.record(home).refillAt());
  }
}
