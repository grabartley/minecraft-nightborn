package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HouseholdLedgerTest {

  private static final UUID VAMPIRE = new UUID(0, 1);
  private static final GlobalPos CRYPT = at(0, 40, 0);
  private static final GlobalPos MANOR = at(1000, 70, 0);
  private static final GlobalPos HEAD_COFFIN = at(2, 40, 1);

  private static GlobalPos at(final int x, final int y, final int z) {
    return GlobalPos.create(World.OVERWORLD, new BlockPos(x, y, z));
  }

  private static HouseholdLedger ledger() {
    final HouseholdLedger ledger = new HouseholdLedger();
    ledger.enlist(CRYPT, VAMPIRE);
    ledger.change(CRYPT, record -> record.headSleepsAt(HEAD_COFFIN));
    ledger.enlist(MANOR, VAMPIRE);
    return ledger;
  }

  @ParameterizedTest(name = "coffin {0}, {1}, {2} within {3} -> {4}")
  @CsvSource({
    "2, 40, 1, 16, crypt",
    "3, 40, 1, 16, none",
    "1005, 70, 0, 16, manor",
    "1030, 70, 0, 16, none",
    "500, 70, 0, 16, none"
  })
  @DisplayName("a coffin leads to its household by its head's coffin, or by nearness when unknown")
  void homeFor(final int x, final int y, final int z, final int reach, final String home) {
    final Optional<GlobalPos> expected =
        switch (home) {
          case "crypt" -> Optional.of(CRYPT);
          case "manor" -> Optional.of(MANOR);
          default -> Optional.empty();
        };
    assertEquals(expected, ledger().homeFor(at(x, y, z), reach));
  }

  @ParameterizedTest(name = "enlist twice {0}")
  @CsvSource({"true", "false"})
  @DisplayName("enlisting reports a change only the first time")
  void enlist(final boolean twice) {
    final HouseholdLedger ledger = new HouseholdLedger();
    assertEquals(true, ledger.enlist(CRYPT, VAMPIRE));
    if (twice) {
      assertEquals(false, ledger.enlist(CRYPT, VAMPIRE));
    }
    assertEquals(true, ledger.forget(CRYPT, VAMPIRE));
    assertEquals(true, ledger.record(CRYPT).gone());
    assertEquals(false, ledger.forget(MANOR, VAMPIRE));
    assertEquals(false, ledger.records().containsKey(MANOR));
  }
}
