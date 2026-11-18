package com.grahambartley.nightborn.servant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ServantChestsTest {

  private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final GlobalPos A = GlobalPos.create(World.OVERWORLD, new BlockPos(1, 64, 1));
  private static final GlobalPos B = GlobalPos.create(World.OVERWORLD, new BlockPos(9, 64, 9));

  @ParameterizedTest(name = "{0} marks")
  @CsvSource({"1, FETCH", "2, DELIVER", "3, CLEARED"})
  @DisplayName("an owner's marks build up as FAM-4 says, whatever servants they lead")
  void marks(final int times, final ChestMark last) {
    final ServantChests chests = new ServantChests();
    ChestMark mark = null;
    for (int i = 0; i < times; i++) {
      mark = chests.mark(OWNER, i == 1 ? B : (i == 2 ? B : A));
    }
    assertEquals(last, mark);
  }

  @ParameterizedTest(name = "fetch {0}, deliver {1}")
  @CsvSource({"true, true", "true, false", "false, true", "false, false"})
  @DisplayName("marks survive a save and load")
  void saves(final boolean fetch, final boolean deliver) {
    final ServantChests chests = new ServantChests();
    final ChestMarks marks =
        new ChestMarks(
            fetch ? Optional.of(A) : Optional.empty(), deliver ? Optional.of(B) : Optional.empty());
    chests.adopt(Map.of(OWNER, marks));
    final ServantChests loaded =
        ServantChests.fromNbt(chests.writeNbt(new NbtCompound(), null), null);
    assertEquals(marks, loaded.marksOf(OWNER));
  }

  @ParameterizedTest(name = "already marked {0}")
  @CsvSource({"true", "false"})
  @DisplayName("older marks are adopted only where the owner has none yet")
  void adopt(final boolean alreadyMarked) {
    final ServantChests chests = new ServantChests();
    if (alreadyMarked) {
      chests.mark(OWNER, B);
    }
    chests.adopt(Map.of(OWNER, new ChestMarks(Optional.of(A), Optional.empty())));
    assertEquals(alreadyMarked ? Optional.of(B) : Optional.of(A), chests.marksOf(OWNER).fetch());
  }

  @ParameterizedTest(name = "forget {0}")
  @CsvSource({"true", "false"})
  @DisplayName("forgetting a chest clears only that mark")
  void forget(final boolean fetchSide) {
    final ServantChests chests = new ServantChests();
    chests.mark(OWNER, A);
    chests.mark(OWNER, B);
    chests.forget(OWNER, fetchSide ? A : B);
    assertEquals(fetchSide ? Optional.empty() : Optional.of(A), chests.marksOf(OWNER).fetch());
    assertEquals(fetchSide ? Optional.of(B) : Optional.empty(), chests.marksOf(OWNER).deliver());
  }
}
