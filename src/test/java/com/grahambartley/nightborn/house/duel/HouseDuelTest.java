package com.grahambartley.nightborn.house.duel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.boss.Arena;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HouseDuelTest {

  static final UUID LORD = UUID.fromString("00000000-0000-0000-0000-000000000001");
  static final UUID CHALLENGER = UUID.fromString("00000000-0000-0000-0000-000000000002");
  static final UUID BYSTANDER = UUID.fromString("00000000-0000-0000-0000-000000000003");

  @ParameterizedTest(name = "{0} -> involved {1}, foe {2}")
  @CsvSource({
    "00000000-0000-0000-0000-000000000001, true, 00000000-0000-0000-0000-000000000002",
    "00000000-0000-0000-0000-000000000002, true, 00000000-0000-0000-0000-000000000001",
    "00000000-0000-0000-0000-000000000003, false,"
  })
  @DisplayName("a duel has two fighters, each the other's only foe")
  void fighters(final UUID who, final boolean involved, final UUID foe) {
    final HouseDuel duel =
        new HouseDuel(LORD, CHALLENGER, World.OVERWORLD, new Arena(BlockPos.ORIGIN, 8));
    assertEquals(involved, duel.involves(who));
    assertEquals(Optional.ofNullable(foe), duel.foe(who));
  }
}
