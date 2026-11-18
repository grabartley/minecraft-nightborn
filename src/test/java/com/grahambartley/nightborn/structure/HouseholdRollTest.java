package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
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
}
