package com.grahambartley.nightborn.skill;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.Identifier;

public final class PowerBar {

  public static final int SLOTS = 5;

  private PowerBar() {}

  public static List<Identifier> slots(
      final TreeProgress progress, final Predicate<Identifier> active) {
    return progress.ownedInOrder().stream().filter(active).limit(SLOTS).toList();
  }
}
