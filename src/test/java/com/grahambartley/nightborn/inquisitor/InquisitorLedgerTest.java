package com.grahambartley.nightborn.inquisitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InquisitorLedgerTest {

  static final UUID AWAY = UUID.fromString("00000000-0000-0000-0000-000000000009");
  static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-00000000000a");

  private static List<InquisitorLedger.Owed> owed(final String names) {
    return Arrays.stream(names.split(" ")).map(InquisitorLedger.Owed::valueOf).toList();
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"END_VISIT", "COME_BACK", "CLEAR_NOTORIETY END_VISIT PAY", "PAY PAY"})
  @DisplayName("what a player is owed while away survives a save and is settled once")
  void settlesOnce(final String names) {
    final InquisitorLedger ledger = new InquisitorLedger();
    ledger.owe(AWAY, owed(names));
    final InquisitorLedger loaded =
        InquisitorLedger.fromNbt(ledger.writeNbt(new NbtCompound(), null), null);
    assertEquals(List.of(), loaded.settle(OTHER));
    assertEquals(owed(names), loaded.settle(AWAY));
    assertEquals(List.of(), loaded.settle(AWAY));
  }
}
