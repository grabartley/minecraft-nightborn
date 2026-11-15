package com.grahambartley.nightborn.skill;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class PowerBar {

  public static final int SLOTS = 5;

  private PowerBar() {}

  public static List<Identifier> slots(
      @Nullable final Registry<Skill> skills, final TreeProgress progress) {
    return skills == null
        ? List.of()
        : slots(progress, id -> skills.get(id) != null && skills.get(id).active());
  }

  public static List<Identifier> slots(
      final TreeProgress progress, final Predicate<Identifier> active) {
    return progress.ownedInOrder().stream().filter(active).limit(SLOTS).toList();
  }
}
