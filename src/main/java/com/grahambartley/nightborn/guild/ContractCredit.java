package com.grahambartley.nightborn.guild;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public final class ContractCredit {

  public record Held(HunterContract contract, int done) {}

  private ContractCredit() {}

  public static OptionalInt slotToAdvance(
      final List<Optional<Held>> inventory, final HunterContract.Stake facts) {
    for (int slot = 0; slot < inventory.size(); slot++) {
      final Optional<Held> held = inventory.get(slot);
      if (held.isPresent()
          && !held.get().contract().finishedAt(held.get().done())
          && held.get().contract().countedBy(facts)) {
        return OptionalInt.of(slot);
      }
    }
    return OptionalInt.empty();
  }
}
