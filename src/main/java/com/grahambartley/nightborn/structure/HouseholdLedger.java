package com.grahambartley.nightborn.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.minecraft.util.math.GlobalPos;

public final class HouseholdLedger {

  private final Map<GlobalPos, HouseholdRecord> records = new HashMap<>();

  public boolean change(final GlobalPos home, final UnaryOperator<HouseholdRecord> change) {
    final HouseholdRecord before = record(home);
    final HouseholdRecord after = change.apply(before);
    if (after.equals(before) && records.containsKey(home)) {
      return false;
    }
    records.put(home, after);
    return true;
  }

  public boolean enlist(final GlobalPos home, final UUID vampire) {
    return change(home, record -> record.enlisted(vampire));
  }

  public boolean forget(final GlobalPos home, final UUID vampire) {
    return records.containsKey(home) && change(home, record -> record.forgot(vampire));
  }

  public boolean known(final GlobalPos home) {
    return records.containsKey(home);
  }

  public HouseholdRecord record(final GlobalPos home) {
    return records.getOrDefault(home, HouseholdRecord.NONE);
  }

  public Optional<GlobalPos> homeOfHeadCoffin(final GlobalPos coffin) {
    return records.entrySet().stream()
        .filter(entry -> entry.getValue().headSleepsIn(coffin))
        .map(Map.Entry::getKey)
        .findFirst();
  }

  public Map<GlobalPos, HouseholdRecord> records() {
    return Map.copyOf(records);
  }
}
