package com.grahambartley.nightborn.nemesis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NemesisLedgerTest {

  static final UUID QUARRY = UUID.fromString("00000000-0000-0000-0000-000000000001");
  static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000002");

  static NemesisEntry entry(final int n) {
    return new NemesisEntry(
        new UUID(0L, 100L + n),
        Identifier.of("nightborn", "vampire"),
        "Name " + n,
        n,
        0,
        new NbtCompound());
  }

  @ParameterizedTest(name = "{0} offered with a limit of {1}: {2} held")
  @CsvSource({"1, 3, 1", "3, 3, 3", "4, 3, 3", "6, 3, 3", "2, 0, 0", "5, 5, 5"})
  @DisplayName("a player never holds more nemeses than the limit")
  void neverPastLimit(final int offered, final int limit, final int held) {
    final NemesisLedger ledger = new NemesisLedger();
    IntStream.range(0, offered).forEach(n -> ledger.add(QUARRY, entry(n), limit));
    assertEquals(held, ledger.of(QUARRY).size());
    assertEquals(0, ledger.of(OTHER).size());
  }

  @ParameterizedTest(name = "{0} held, ends {1}")
  @CsvSource({"3, 0, 2", "1, 0, 0", "2, 5, 2"})
  @DisplayName("an ended nemesis leaves the ledger and frees a place")
  void removes(final int held, final int ended, final int left) {
    final NemesisLedger ledger = new NemesisLedger();
    IntStream.range(0, held).forEach(n -> ledger.add(QUARRY, entry(n), 3));
    ledger.remove(QUARRY, entry(ended).mob());
    assertEquals(left, ledger.of(QUARRY).size());
    assertEquals(Optional.empty(), ledger.entry(QUARRY, entry(ended).mob()));
  }

  @ParameterizedTest(name = "{0} held")
  @CsvSource({"0", "1", "3"})
  @DisplayName("a quarry who changes side is forgotten by every nemesis at once")
  void forgets(final int held) {
    final NemesisLedger ledger = new NemesisLedger();
    IntStream.range(0, held).forEach(n -> ledger.add(QUARRY, entry(n), 3));
    assertEquals(held, ledger.forget(QUARRY).size());
    assertEquals(List.of(), ledger.of(QUARRY));
    assertEquals(List.of(), ledger.forget(QUARRY));
  }

  @ParameterizedTest(name = "visit {0} with token {1}")
  @CsvSource({"40, 1", "9, 3"})
  @DisplayName("grudges and growth survive a save, and a damaged player is skipped")
  void survivesSave(final long next, final int token) {
    final NemesisLedger ledger = new NemesisLedger();
    ledger.add(QUARRY, entry(1), 3);
    ledger.add(QUARRY, entry(2), 3);
    ledger.put(QUARRY, entry(2).visited(next, token));
    final NbtCompound nbt = ledger.writeNbt(new NbtCompound(), null);
    nbt.put("not-a-player", new NbtList());
    final NemesisLedger loaded = NemesisLedger.fromNbt(nbt, null);
    assertEquals(ledger.of(QUARRY), loaded.of(QUARRY));
    assertEquals(Optional.of(entry(2).visited(next, token)), loaded.entry(QUARRY, entry(2).mob()));
    assertEquals(Set.of(QUARRY), loaded.quarries());
  }

  @ParameterizedTest(name = "{0} held, ends {1}, the rest forgotten")
  @CsvSource({"3, 0", "2, 1"})
  @DisplayName("an ended nemesis and a forgotten one are remembered through a save")
  void remembersEndedAndReleased(final int held, final int ended) {
    final NemesisLedger ledger = new NemesisLedger();
    IntStream.range(0, held).forEach(n -> ledger.add(QUARRY, entry(n).visited(n, n + 1), 3));
    ledger.end(QUARRY, entry(ended).mob());
    ledger.forget(QUARRY);
    final NemesisLedger loaded =
        NemesisLedger.fromNbt(ledger.writeNbt(new NbtCompound(), null), null);
    IntStream.range(0, held)
        .forEach(
            n -> {
              assertEquals(n == ended, loaded.ended(entry(n).mob()));
              assertEquals(
                  n == ended ? Optional.empty() : Optional.of(n + 1),
                  loaded.released(entry(n).mob()));
            });
    assertEquals(Set.of(), loaded.quarries());
  }
}
